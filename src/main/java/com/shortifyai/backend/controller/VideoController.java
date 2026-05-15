package com.shortifyai.backend.controller;

import com.shortifyai.backend.entity.Job;
import com.shortifyai.backend.service.JobService;
import com.shortifyai.backend.service.VideoProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final JobService jobService;
    private final VideoProcessingService videoProcessingService;

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            File directory = new File(uploadDir);

            if (!directory.exists()) {
                boolean isCreated = directory.mkdirs();
                if (!isCreated) {
                    throw new RuntimeException("Failed to create upload directory.");
                }
            }

            String originalFileName = file.getOriginalFilename();
            String cleanFileName = originalFileName.replaceAll("\\s+", "_");

            String filePath = uploadDir + System.currentTimeMillis() + "_" + cleanFileName;

            File dest = new File(filePath);
            file.transferTo(dest);

            // 4. Create job
            Job job = jobService.createJob(filePath);

            // 🔥 CALL ASYNC PROCESSING
            videoProcessingService.processVideo(job.getJobId());

            // 5. Return response
            return ResponseEntity.ok().body(
                    java.util.Map.of(
                            "status", "success",
                            "message", "Video uploaded successfully",
                            "jobId", job.getJobId()
                    )
            );

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload video. Please try again.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong.");
        }
    }


    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> getJobStatus(@PathVariable String jobId) {

        try {
            return jobService.getJobById(jobId)
                    .map(job -> {

                        java.util.Map<String, Object> response = new java.util.HashMap<>();

                        response.put("jobId", job.getJobId());
                        response.put("status", job.getStatus() != null ? job.getStatus().name() : "UNKNOWN");
                        response.put("progress", job.getProgress());
                        response.put("clips", job.getOutputClips() != null ? job.getOutputClips() : java.util.List.of());
                        response.put("error", job.getErrorMessage() != null ? job.getErrorMessage() : "");

                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(java.util.Map.of(
                                    "error", "Job not found",
                                    "jobId", jobId
                            ))
                    );

        } catch (Exception e) {

            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", e.getMessage());

            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/download/{jobId}")
    public ResponseEntity<?> downloadAllClips(@PathVariable String jobId) {

        try {
            Optional<Job> optionalJob = jobService.getJobById(jobId);

            if (optionalJob.isEmpty()) {
                return ResponseEntity.status(404).body("Job not found");
            }

            Job job = optionalJob.get();

            if (job.getOutputClips() == null || job.getOutputClips().isEmpty()) {
                return ResponseEntity.status(404).body("No clips found");
            }

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

            for (String path : job.getOutputClips()) {

                File file = new File(path);

                if (!file.exists()) continue;

                zos.putNextEntry(new java.util.zip.ZipEntry(file.getName()));
                java.nio.file.Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }

            zos.close();

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=clips_" + jobId + ".zip")
                    .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating zip");
        }
    }
}