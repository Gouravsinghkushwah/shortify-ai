package com.shortifyai.backend.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortifyai.backend.entity.Job;
import com.shortifyai.backend.entity.JobStatus;
import com.shortifyai.backend.repository.JobRepository;
import com.shortifyai.backend.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoProcessingServiceImpl implements VideoProcessingService {

    private final JobRepository jobRepository;

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d-%02d", min, sec);
    }

    @Async("videoExecutor")
    @Override
    public void processVideo(String jobId) {

        Job job = null;

        try {
            job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            log.info("Processing job: {}", jobId);

            job.setStatus(JobStatus.PROCESSING);
            job.setProgress(10);
            jobRepository.save(job);

            // =========================
            // RUN PYTHON (SAFE VERSION)
            // =========================
            String pythonPath = "python"; // OR full path if needed

            String scriptPath = System.getProperty("user.dir")
                    + "\\scripts\\highlight_detector.py";

//            String videoPath = job.getInputVideoPath();
            String videoPath = job.getInputVideoPath()
                    .replace("\\", "/");   // Windows fix


            File f = new File(videoPath);
            log.info("FILE EXISTS? => {}", f.exists());


            log.info("VIDEO PATH => {}", videoPath);
            log.info("SCRIPT PATH => {}", scriptPath);
            log.info("VIDEO PATH SENT TO PYTHON => {}", videoPath);
//            ProcessBuilder pb = new ProcessBuilder(
//                    pythonPath,
//                    scriptPath,
//                    videoPath
//            );

            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe", "/c",
                    pythonPath,
                    "\"" + scriptPath + "\"",
                    "\"" + videoPath + "\""
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            StringBuilder outputBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println("PYTHON >>> " + line); // 🔥 DEBUG
                outputBuilder.append(line);
            }

            int exitCode = process.waitFor();

            String output = outputBuilder.toString().trim();

            System.out.println("PYTHON EXIT CODE => " + exitCode);
            System.out.println("PYTHON FINAL OUTPUT => " + output);

            // =========================
            // ERROR HANDLING (VERY IMPORTANT)
            // =========================
            if (output.isEmpty()) {
                throw new RuntimeException("Python returned EMPTY output");
            }

            if (exitCode != 0) {
                throw new RuntimeException("Python failed with exit code: " + exitCode + " | Output: " + output);
            }

            // =========================
            // EXTRACT JSON ONLY (FIX)
            // =========================
            int startIndex = output.indexOf("{");
            int endIndex = output.lastIndexOf("}");

            if (startIndex == -1 || endIndex == -1) {
                throw new RuntimeException("Invalid Python output (no JSON): " + output);
            }

            String json = output.substring(startIndex, endIndex + 1);

            log.info("CLEAN JSON => {}", json);

            // =========================
            // PARSE JSON
            // =========================
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            JsonNode tsNode = root.path("timestamps");

            if (!tsNode.isArray() || tsNode.size() == 0) {
                throw new RuntimeException("No timestamps returned from AI script");
            }

            // =========================
            // CONVERT TIMESTAMPS
            // =========================
            List<Double> rawTimes = new ArrayList<>();
            for (JsonNode n : tsNode) {
                rawTimes.add(n.asDouble());
            }

            log.info("RAW TIMESTAMPS => {}", rawTimes);

            // =========================
            // FILTER (MIN GAP 5 SEC)
            // =========================
            List<Double> filtered = new ArrayList<>();
            double last = -999;

            for (Double t : rawTimes) {
                if (t - last >= 5) {
                    filtered.add(t);
                    last = t;
                }
            }

            log.info("FILTERED TIMESTAMPS => {}", filtered);

            // =========================
            // GENERATE CLIPS
            // =========================
            List<String> outputClips = new ArrayList<>();

            String baseName = new java.io.File(job.getInputVideoPath())
                    .getName()
                    .replace(".mp4", "");

            for (Double time : filtered) {

                int start = time.intValue();
                int end = start + 30;

                String fileName = baseName + "_"
                        + formatTime(start)
                        + "_to_"
                        + formatTime(end)
                        + ".mp4";

                String outputFile = System.getProperty("user.dir")
                        + "/uploads/" + fileName;

                ProcessBuilder ffmpeg = new ProcessBuilder(
                        "ffmpeg",
                        "-y",
                        "-i", job.getInputVideoPath(),
                        "-ss", String.valueOf(start),
                        "-t", "30",
                        "-vf", "scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920",
                        "-c:v", "libx264",
                        "-preset", "fast",
                        "-crf", "23",
                        "-c:a", "aac",
                        outputFile
                );

                ffmpeg.redirectErrorStream(true);

                Process ff = ffmpeg.start();
                int exit = ff.waitFor();

                if (exit == 0) {
                    log.info("Clip created: {}", outputFile);
                    outputClips.add(outputFile);
                } else {
                    log.error("FFmpeg failed at {}", start);
                }
            }

            // =========================
            // SAVE RESULT
            // =========================
            job.setOutputClips(outputClips);
            job.setStatus(JobStatus.COMPLETED);
            job.setProgress(100);
            jobRepository.save(job);

        } catch (Exception e) {

            log.error("Processing failed for jobId={}", jobId, e);

            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage(e.getMessage());
                jobRepository.save(job);
            }
        }
    }
}