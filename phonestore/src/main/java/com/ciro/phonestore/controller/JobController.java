package com.ciro.phonestore.controller;

import com.ciro.phonestore.exceptions.JobNotFoundException;
import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import com.ciro.phonestore.services.JobService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private JobService jobService;

    @GetMapping("/public/{jobNumber}")
    public ResponseEntity<Job> getJobByNumberPublic(@PathVariable String jobNumber) {
        return ResponseEntity.ok(jobService.getJobByNumber(jobNumber));
    }

    @GetMapping("/{jobNumber}")
    public ResponseEntity<Job> getJobByNumber(@PathVariable String jobNumber) {
        return ResponseEntity.ok(jobService.getJobByNumber(jobNumber));
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @PostMapping("/create")
    public ResponseEntity<Job> createJob(@Valid @RequestBody Job job) {
        return ResponseEntity.ok(jobService.createJob(job));
    }

    @PutMapping("/update/{jobNumber}")
    public ResponseEntity<?> updateJobStatus(
            @PathVariable String jobNumber,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            JobStatus newStatus = JobStatus.valueOf(statusUpdate.get("status"));
            Job updatedJob = jobService.updateJobStatus(jobNumber, newStatus);
            return ResponseEntity.ok(updatedJob);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid status value. Allowed values are: IN_QUEUE, IN_PROGRESS, COMPLETED"));
        }
    }

    @DeleteMapping("/delete/{jobNumber}")
    public ResponseEntity<?> deleteJob(@PathVariable String jobNumber) {
        logger.info("Received delete request for job number: {}", jobNumber);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("User attempting delete: {}, Authorities: {}",
                auth.getName(),
                auth.getAuthorities());

        try {
            jobService.deleteJob(jobNumber);
            logger.info("Successfully deleted job: {}", jobNumber);
            return ResponseEntity.ok(Map.of("message", "Job " + jobNumber + " deleted successfully"));
        } catch (JobNotFoundException e) {
            logger.warn("Job not found for deletion: {}", jobNumber);
            return ResponseEntity.notFound()
                    .build();
        } catch (Exception e) {
            logger.error("Error deleting job {}: {}", jobNumber, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to delete job: " + e.getMessage()));
        }
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<?> handleJobNotFoundException(JobNotFoundException e) {
        return ResponseEntity.notFound()
                .build();
    }
}