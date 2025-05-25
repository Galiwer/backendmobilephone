package com.ciro.phonestore.controller;

import com.ciro.phonestore.exceptions.JobNotFoundException;
import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import com.ciro.phonestore.services.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

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
    public ResponseEntity<Void> deleteJob(@PathVariable String jobNumber) {
        jobService.deleteJob(jobNumber);
        return ResponseEntity.ok().build();
    }
}