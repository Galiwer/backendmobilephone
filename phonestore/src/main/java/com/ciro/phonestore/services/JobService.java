package com.ciro.phonestore.services;

import com.ciro.phonestore.exceptions.JobNotFoundException;
import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import com.ciro.phonestore.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobRepository jobRepository;

    public Job getJobByNumber(String jobNumber) {
        return jobRepository.findById(jobNumber)
                .orElseThrow(() -> new JobNotFoundException(jobNumber));
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job createJob(Job job) {
        job.setStatus(JobStatus.IN_QUEUE);
        job.setQueueDate(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public Job updateJobStatus(String jobNumber, JobStatus newStatus) {
        Job job = getJobByNumber(jobNumber);
        job.setStatus(newStatus);

        // Update timestamps based on status
        switch (newStatus) {
            case IN_QUEUE:
                job.setQueueDate(LocalDateTime.now());
                break;
            case IN_PROGRESS:
                job.setProcessingDate(LocalDateTime.now());
                break;
            case COMPLETED:
                job.setDoneDate(LocalDateTime.now());
                break;
        }

        return jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(String jobNumber) {
        logger.info("Attempting to delete job with number: {}", jobNumber);

        Job job = jobRepository.findById(jobNumber)
                .orElseThrow(() -> {
                    logger.warn("Job not found for deletion with number: {}", jobNumber);
                    return new JobNotFoundException(jobNumber);
                });

        try {
            logger.info("Found job to delete: {}, current status: {}", jobNumber, job.getStatus());
            jobRepository.delete(job);
            logger.info("Successfully deleted job with number: {}", jobNumber);
        } catch (Exception e) {
            logger.error("Error deleting job with number {}: {}", jobNumber, e.getMessage(), e);
            throw new RuntimeException("Failed to delete job: " + e.getMessage());
        }
    }
}