package com.shortifyai.backend.service;

import com.shortifyai.backend.entity.Job;

import java.util.Optional;

public interface JobService {

    Job createJob(String inputVideoPath);

    Optional<Job> getJobById(String jobId);

    Job updateJob(Job job);
}