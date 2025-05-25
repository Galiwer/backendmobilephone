package com.ciro.phonestore.services;

import com.ciro.phonestore.exceptions.JobNotFoundException;
import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import com.ciro.phonestore.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

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

    public void deleteJob(String jobNumber) {
        if (!jobRepository.existsById(jobNumber)) {
            throw new JobNotFoundException(jobNumber);
        }
        jobRepository.deleteById(jobNumber);
    }
}