package com.shortifyai.backend.serviceImpl;

import com.shortifyai.backend.entity.Job;
import com.shortifyai.backend.entity.JobStatus;
import com.shortifyai.backend.repository.JobRepository;
import com.shortifyai.backend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    @Override
    public Job createJob(String inputVideoPath) {
        try {
            Job job = Job.builder()
                    .jobId(UUID.randomUUID().toString())
                    .status(JobStatus.PENDING)
                    .progress(0)
                    .inputVideoPath(inputVideoPath)
                    .build();
            log.info("Creating job for video: {}", inputVideoPath);
            return jobRepository.save(job);


        } catch (Exception e) {
            throw new RuntimeException("Failed to create job. Please try again.");
        }
    }

    @Override
    public Optional<Job> getJobById(String jobId) {
        try {
            log.info("Fetching job: {}", jobId);
            return jobRepository.findById(jobId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch job details. Please try again.");
        }
    }

    @Override
    public Job updateJob(Job job) {
        try {
            log.info("Updating job: {}", job.getJobId());
            return jobRepository.save(job);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update job status.");
        }
    }
}