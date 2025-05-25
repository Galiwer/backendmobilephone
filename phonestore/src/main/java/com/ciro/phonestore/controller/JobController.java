package com.ciro.phonestore.controller;

import com.ciro.phonestore.exceptions.JobNotFoundException;
import com.ciro.phonestore.exceptions.InvalidJobNumberFormatException;
import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import com.ciro.phonestore.repository.JobRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/public/{jobNumber}")
    @Cacheable(value = "jobs", key = "#jobNumber")
    public ResponseEntity<Job> getJobByNumberPublic(@PathVariable String jobNumber) {
        validateJobNumber(jobNumber);
        return jobRepository.findById(jobNumber)
                .map(job -> ResponseEntity.ok()
                        .body(job))
                .orElseThrow(() -> new JobNotFoundException("Job not found with number: " + jobNumber));
    }

    @GetMapping("/{jobNumber}")
    @Cacheable(value = "jobs", key = "#jobNumber")
    public ResponseEntity<Job> getJobByNumber(@PathVariable String jobNumber) {
        validateJobNumber(jobNumber);
        return jobRepository.findById(jobNumber)
                .map(job -> ResponseEntity.ok()
                        .body(job))
                .orElseThrow(() -> new JobNotFoundException("Job not found with number: " + jobNumber));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "jobNumber") String sortBy) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Job> jobPage = jobRepository.findAll(pageRequest);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Jobs retrieved successfully",
                "data", jobPage.getContent(),
                "currentPage", jobPage.getNumber(),
                "totalItems", jobPage.getTotalElements(),
                "totalPages", jobPage.getTotalPages()));
    }

    @PostMapping("/create")
    @CacheEvict(value = "jobs", allEntries = true)
    public ResponseEntity<Map<String, Object>> createJob(@Valid @RequestBody Job job) {
        validateJobNumber(job.getJobNumber());

        job.setStatus(JobStatus.IN_QUEUE);
        job.setQueueDate(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "status", "success",
                        "message", "Job created successfully",
                        "data", savedJob));
    }

    @PutMapping("/update/{jobNumber}")
    @CacheEvict(value = "jobs", key = "#jobNumber")
    public ResponseEntity<Map<String, Object>> updateJobStatus(
            @PathVariable String jobNumber,
            @RequestBody Map<String, String> statusUpdate) {
        validateJobNumber(jobNumber);

        Job job = jobRepository.findById(jobNumber)
                .orElseThrow(() -> new JobNotFoundException("Job not found with number: " + jobNumber));

        try {
            JobStatus newStatus = JobStatus.valueOf(statusUpdate.get("status"));
            job.setStatus(newStatus);

            switch (newStatus) {
                case IN_QUEUE -> job.setQueueDate(LocalDateTime.now());
                case IN_PROGRESS -> job.setProcessingDate(LocalDateTime.now());
                case COMPLETED -> job.setDoneDate(LocalDateTime.now());
            }

            Job updatedJob = jobRepository.save(job);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Job status updated successfully",
                    "data", updatedJob));
        } catch (IllegalArgumentException e) {
            throw new InvalidJobNumberFormatException(
                    "Invalid status value. Allowed values are: IN_QUEUE, IN_PROGRESS, COMPLETED");
        }
    }

    @DeleteMapping("/delete/{jobNumber}")
    @CacheEvict(value = "jobs", allEntries = true)
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable String jobNumber) {
        validateJobNumber(jobNumber);

        if (!jobRepository.existsById(jobNumber)) {
            throw new JobNotFoundException("Job not found with number: " + jobNumber);
        }

        jobRepository.deleteById(jobNumber);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Job " + jobNumber + " deleted successfully"));
    }

    private void validateJobNumber(String jobNumber) {
        if (jobNumber == null || !jobNumber.matches("^J[1-9]\\d*$")) {
            throw new InvalidJobNumberFormatException(
                    "Job number must be in format 'J' followed by a number (e.g., J1, J2, J3)");
        }
    }
}