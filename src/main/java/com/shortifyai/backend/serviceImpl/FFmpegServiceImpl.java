package com.shortifyai.backend.serviceImpl;

import com.shortifyai.backend.service.FFmpegService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FFmpegServiceImpl implements FFmpegService {

    @Override
    public List<String> generateClips(String inputPath, String outputDir, List<Integer> timestamps) {

        List<String> outputFiles = new ArrayList<>();

        try {
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                throw new RuntimeException("Input video not found: " + inputPath);
            }

            File dir = new File(outputDir);
            if (!dir.exists()) dir.mkdirs();

            for (Integer start : timestamps) {

                String outputFile = outputDir + File.separator + "clip_" + start + ".mp4";

                log.info("Starting FFmpeg for timestamp: {}", start);

                ProcessBuilder pb = new ProcessBuilder(
                        "ffmpeg",
                        "-y",
                        "-i", inputPath,
                        "-ss", String.valueOf(start),
                        "-t", "30",
                        "-vf", "scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920",
                        "-c:v", "libx264",
                        "-preset", "fast",
                        "-crf", "23",
                        "-c:a", "aac",
                        outputFile
                );



                pb.redirectErrorStream(true);

                Process process = pb.start();

                // 🔥 SAFELY READ STREAM (NO HANG)
                StringBuilder ffmpegLog = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        ffmpegLog.append(line).append("\n");
                        log.info("FFMPEG: {}", line);
                    }
                }

                int exitCode = process.waitFor();

                log.info("FFmpeg exit code for {}: {}", start, exitCode);

                if (exitCode != 0) {
                    log.error("FFmpeg FAILED at {} | LOG:\n{}", start, ffmpegLog);
                    throw new RuntimeException("FFmpeg failed at timestamp: " + start);
                }



                outputFiles.add(outputFile);
                log.info("Clip generated successfully: {}", outputFile);
            }

        } catch (Exception e) {
            log.error("FFmpeg processing error", e);
            throw new RuntimeException("FFmpeg processing failed", e);
        }

        return outputFiles;
    }
}