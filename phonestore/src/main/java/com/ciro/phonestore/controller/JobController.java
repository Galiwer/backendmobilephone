package com.ciro.phonestore.controller;

import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import com.ciro.phonestore.services.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    // Public endpoint for job tracking
    @GetMapping("/public/{jobNumber}")
    public ResponseEntity<Job> getJobByNumberPublic(@PathVariable String jobNumber) {
        return jobRepository.findById(jobNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET job by job number (admin only)
    @GetMapping("/{jobNumber}")
    public ResponseEntity<Job> getJobByNumber(@PathVariable String jobNumber) {
        return jobRepository.findById(jobNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET all jobs (for admin view)
    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobRepository.findAll());
    }

    // POST a new job
    @PostMapping("/create")
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        // Set default status to IN_QUEUE when a job is created
        job.setStatus(JobStatus.IN_QUEUE);
        job.setQueueDate(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);
        return ResponseEntity.ok(savedJob);
    }

    // PUT update job status
    @PutMapping("/update/{jobNumber}")
    public ResponseEntity<?> updateJobStatus(@PathVariable String jobNumber,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            Job job = jobRepository.findById(jobNumber)
                    .orElseThrow(() -> new RuntimeException("Job not found: " + jobNumber));

            JobStatus newStatus = JobStatus.valueOf(statusUpdate.get("status"));
            job.setStatus(newStatus);

            switch (newStatus) {
                case IN_QUEUE -> job.setQueueDate(LocalDateTime.now());
                case IN_PROGRESS -> job.setProcessingDate(LocalDateTime.now());
                case COMPLETED -> job.setDoneDate(LocalDateTime.now());
            }

            Job updatedJob = jobRepository.save(job);
            return ResponseEntity.ok(updatedJob);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid status value. Allowed values are: IN_QUEUE, IN_PROGRESS, COMPLETED"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE a job by job number
    @DeleteMapping("/delete/{jobNumber}")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable String jobNumber) {
        if (!jobRepository.existsById(jobNumber)) {
            return ResponseEntity.notFound().build();
        }
        jobRepository.deleteById(jobNumber);
        return ResponseEntity.ok(Map.of("message", "Job " + jobNumber + " deleted successfully"));
    }
}