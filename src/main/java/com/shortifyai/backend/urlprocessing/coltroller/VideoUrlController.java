package com.shortifyai.backend.urlprocessing.coltroller;

import com.shortifyai.backend.urlprocessing.service.VideoUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/url-video")
@RequiredArgsConstructor
public class VideoUrlController {

    private final VideoUrlService videoUrlService;

    @PostMapping("/process")
    public ResponseEntity<?> processVideoFromUrl(@RequestBody Map<String, String> request) {

        try {
            String url = request.get("url");

            if (url == null || url.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "status", "failed",
                                "message", "URL is required"
                        )
                );
            }

            String jobId = videoUrlService.processUrlVideo(url);

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "Video processing started",
                            "jobId", jobId
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "status", "failed",
                            "message", e.getMessage()
                    )
            );
        }
    }
}