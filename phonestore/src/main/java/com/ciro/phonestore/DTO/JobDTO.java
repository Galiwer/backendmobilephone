package com.ciro.phonestore.DTO;

import com.ciro.phonestore.models.Job;
import com.ciro.phonestore.models.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class JobDTO {
    @NotBlank(message = "Job number is required")
    @Pattern(regexp = "^[Jj]\\d+$", message = "Job number must start with 'J' or 'j' followed by numbers")
    private String jobNumber;

    @NotNull(message = "Job status is required")
    private JobStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime queueDate;
    private LocalDateTime processingDate;
    private LocalDateTime doneDate;

    // Default constructor
    public JobDTO() {
    }

    // Constructor from Job entity
    public JobDTO(Job job) {
        this.jobNumber = job.getJobNumber();
        this.status = job.getStatus();
        this.createdAt = job.getCreatedAt();
        this.queueDate = job.getQueueDate();
        this.processingDate = job.getProcessingDate();
        this.doneDate = job.getDoneDate();
    }

    // Convert DTO to Entity
    public Job toEntity() {
        Job job = new Job();
        job.setJobNumber(this.jobNumber);
        job.setStatus(this.status);
        job.setCreatedAt(this.createdAt != null ? this.createdAt : LocalDateTime.now());
        job.setQueueDate(this.queueDate);
        job.setProcessingDate(this.processingDate);
        job.setDoneDate(this.doneDate);
        return job;
    }

    // Getters and Setters
    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        this.jobNumber = jobNumber;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getQueueDate() {
        return queueDate;
    }

    public void setQueueDate(LocalDateTime queueDate) {
        this.queueDate = queueDate;
    }

    public LocalDateTime getProcessingDate() {
        return processingDate;
    }

    public void setProcessingDate(LocalDateTime processingDate) {
        this.processingDate = processingDate;
    }

    public LocalDateTime getDoneDate() {
        return doneDate;
    }

    public void setDoneDate(LocalDateTime doneDate) {
        this.doneDate = doneDate;
    }
}