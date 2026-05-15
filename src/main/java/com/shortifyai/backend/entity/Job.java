package com.shortifyai.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    private String jobId;

    private JobStatus  status; // PENDING, PROCESSING, COMPLETED, FAILED

    private Integer progress; // 0 to 100

    private String inputVideoPath;

    private List<String> outputClips;

    private List<String> outputVideoPaths;

    private String errorMessage;
}