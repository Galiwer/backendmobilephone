package com.ciro.phonestore.controller;

import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.services.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    // GET job by job number
    @GetMapping("/{jobNumber}")
    public Job getJobByNumber(@PathVariable String jobNumber) {
        return jobRepository.findById(jobNumber)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobNumber));
    }

    // GET all jobs (for admin view)
    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // POST a new job
    @PostMapping("/create")
    public Job createJob(@RequestBody Job job) {
        // Set default status to 'In Queue' (1) when a job is created
        job.setStatus(1);
        job.setQueueDate(LocalDateTime.now()); // Set the queueDate to the current time

        // Save the job to the repository
        return jobRepository.save(job);
    }

    // PUT update job status
    @PutMapping("/update/{jobNumber}")
    public Job updateJobStatus(@PathVariable String jobNumber, @RequestBody Integer newStatus) {
        Job job = jobRepository.findById(jobNumber)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobNumber));

        job.setStatus(newStatus);

        switch (newStatus) {
            case 1 -> job.setQueueDate(LocalDateTime.now());
            case 2 -> job.setProcessingDate(LocalDateTime.now());
            case 3 -> job.setDoneDate(LocalDateTime.now());
        }

        return jobRepository.save(job);
    }

    // DELETE a job by job number
    @DeleteMapping("/delete/{jobNumber}")
    public String deleteJob(@PathVariable String jobNumber) {
        jobRepository.deleteById(jobNumber);
        return "Job " + jobNumber + " deleted";
    }
}