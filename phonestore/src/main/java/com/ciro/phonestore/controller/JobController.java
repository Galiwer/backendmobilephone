package com.ciro.phonestore.controller;

import com.ciro.phonestore.exceptions.InvalidJobNumberException;
import com.ciro.phonestore.exceptions.JobNotFoundException;
import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import com.ciro.phonestore.repository.JobRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/public/{jobNumber}")
    public ResponseEntity<Map<String, Object>> getJobByNumberPublic(@PathVariable String jobNumber) {
        validateJobNumber(jobNumber);
        Job job = jobRepository.findById(jobNumber)
                .orElseThrow(() -> new JobNotFoundException(jobNumber));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", job);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobNumber}")
    public ResponseEntity<Map<String, Object>> getJobByNumber(@PathVariable String jobNumber) {
        validateJobNumber(jobNumber);
        Job job = jobRepository.findById(jobNumber)
                .orElseThrow(() -> new JobNotFoundException(jobNumber));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", job);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", jobs);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createJob(@Valid @RequestBody Job job, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", bindingResult.getFieldError().getDefaultMessage());
            return ResponseEntity.badRequest().body(response);
        }

        validateJobNumber(job.getJobNumber());

        job.setStatus(JobStatus.IN_QUEUE);
        job.setQueueDate(LocalDateTime.now());

        Job savedJob = jobRepository.save(job);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Job created successfully");
        response.put("data", savedJob);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{jobNumber}")
    public ResponseEntity<Map<String, Object>> updateJobStatus(
            @PathVariable String jobNumber,
            @RequestBody Map<String, String> statusUpdate) {

        validateJobNumber(jobNumber);
        Job job = jobRepository.findById(jobNumber)
                .orElseThrow(() -> new JobNotFoundException(jobNumber));

        try {
            JobStatus newStatus = JobStatus.valueOf(statusUpdate.get("status"));
            job.setStatus(newStatus);

            switch (newStatus) {
                case IN_QUEUE -> job.setQueueDate(LocalDateTime.now());
                case IN_PROGRESS -> job.setProcessingDate(LocalDateTime.now());
                case COMPLETED -> job.setDoneDate(LocalDateTime.now());
            }

            Job updatedJob = jobRepository.save(job);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Job status updated successfully");
            response.put("data", updatedJob);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid status value. Allowed values are: IN_QUEUE, IN_PROGRESS, COMPLETED");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/delete/{jobNumber}")
    public ResponseEntity<Map<String, Object>> deleteJob(@PathVariable String jobNumber) {
        validateJobNumber(jobNumber);
        if (!jobRepository.existsById(jobNumber)) {
            throw new JobNotFoundException(jobNumber);
        }

        jobRepository.deleteById(jobNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Job " + jobNumber + " deleted successfully");
        return ResponseEntity.ok(response);
    }

    private void validateJobNumber(String jobNumber) {
        if (!jobNumber.matches("^J[1-3]$")) {
            throw new InvalidJobNumberException("Job number must be in format J1, J2, or J3");
        }
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleJobNotFoundException(JobNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidJobNumberException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJobNumberException(InvalidJobNumberException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}