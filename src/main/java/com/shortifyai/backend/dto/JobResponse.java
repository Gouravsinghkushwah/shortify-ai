package com.shortifyai.backend.dto;

import com.shortifyai.backend.entity.JobStatus;
import lombok.Data;

import java.util.List;

@Data
public class JobResponse {
    private String jobId;
    private JobStatus status;
    private int progress;
    private List<String> clips;
}