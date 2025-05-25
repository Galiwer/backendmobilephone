package com.ciro.phonestore.controller;

import com.ciro.phonestore.exceptions.JobNotFoundException;
import com.ciro.phonestore.exceptions.InvalidJobNumberException;
import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import com.ciro.phonestore.repository.JobRepository;
import jakarta.validation.Valid;
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

    @GetMapping("/public/{jobNumber}")
    public ResponseEntity<Job> getJobByNumberPublic(@PathVariable String jobNumber) {
        return ResponseEntity.ok(jobRepository.findById(jobNumber)
                .orElseThrow(() -> new JobNotFoundException(jobNumber)));
    }

    @GetMapping("/{jobNumber}")
    public ResponseEntity<Job> getJobByNumber(@PathVariable String jobNumber) {
        return ResponseEntity.ok(jobRepository.findById(jobNumber)
                .orElseThrow(() -> new JobNotFoundException(jobNumber)));
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobRepository.findAll());
    }

    @PostMapping("/create")
    public ResponseEntity<Job> createJob(@Valid @RequestBody Job job) {
        job.setStatus(JobStatus.IN_QUEUE);
        job.setQueueDate(LocalDateTime.now());
        Job savedJob = jobRepository.save(job);
        return ResponseEntity.ok(savedJob);
    }

    @PutMapping("/update/{jobNumber}")
    public ResponseEntity<?> updateJobStatus(@PathVariable String jobNumber,
            @Valid @RequestBody Map<String, String> statusUpdate) {
        try {
            Job job = jobRepository.findById(jobNumber)
                    .orElseThrow(() -> new JobNotFoundException(jobNumber));

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
                            "Error: Invalid status value. Allowed values are: IN_QUEUE, IN_PROGRESS, COMPLETED"));
        }
    }

    @DeleteMapping("/delete/{jobNumber}")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable String jobNumber) {
        if (!jobRepository.existsById(jobNumber)) {
            throw new JobNotFoundException(jobNumber);
        }
        jobRepository.deleteById(jobNumber);
        return ResponseEntity.ok(Map.of("message", "Success: Job " + jobNumber + " deleted successfully"));
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleJobNotFoundException(JobNotFoundException e) {
        return ResponseEntity.status(404)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InvalidJobNumberException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJobNumberException(InvalidJobNumberException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }
}