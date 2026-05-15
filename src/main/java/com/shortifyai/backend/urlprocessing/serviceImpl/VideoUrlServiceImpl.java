package com.shortifyai.backend.urlprocessing.serviceImpl;

import com.shortifyai.backend.entity.Job;
import com.shortifyai.backend.entity.JobStatus;
import com.shortifyai.backend.repository.JobRepository;
import com.shortifyai.backend.service.VideoProcessingService;
import com.shortifyai.backend.urlprocessing.service.VideoUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoUrlServiceImpl implements VideoUrlService {

    private final JobRepository jobRepository;
    private final VideoProcessingService videoProcessingService;

    @Override
    public String processUrlVideo(String url) {

        try {
            // STEP 1: Job ID
            String jobId = UUID.randomUUID().toString();

            // STEP 2: Download dir
            String downloadDir = System.getProperty("user.dir") + "/downloads/";
            File dir = new File(downloadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String outputTemplate = downloadDir + jobId + ".%(ext)s";

            // STEP 3: RUN yt-dlp
            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Users\\Gourav\\AppData\\Roaming\\Python\\Python314\\Scripts\\yt-dlp.exe",
                    "--cookies", "cookies.txt",
                    "--user-agent", "Mozilla/5.0",
                    "--no-playlist",
                    "-o", outputTemplate,
                    url
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String logOutput = reader.lines()
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                log.error("yt-dlp failed: {}", logOutput);
                throw new RuntimeException("Video download failed");
            }

            log.info("yt-dlp SUCCESS");

            // STEP 4: 🔥 NOW FIND FILE (AFTER DOWNLOAD)
            File[] files = dir.listFiles((d, name) -> name.startsWith(jobId));

            if (files == null || files.length == 0) {
                throw new RuntimeException("Downloaded file not found");
            }

            String actualVideoPath = files[0].getAbsolutePath();

            log.info("ACTUAL VIDEO FILE => {}", actualVideoPath);

            // STEP 5: SAVE JOB
            Job job = Job.builder()
                    .jobId(jobId)
                    .inputVideoPath(actualVideoPath)
                    .status(JobStatus.PENDING)
                    .progress(0)
                    .build();

            jobRepository.save(job);

            // STEP 6: PROCESS VIDEO
            videoProcessingService.processVideo(jobId);

            return jobId;

        } catch (Exception e) {
            log.error("URL processing failed", e);
            throw new RuntimeException("Failed to process video URL: " + e.getMessage());
        }
    }
}