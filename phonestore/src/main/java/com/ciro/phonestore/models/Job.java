package com.ciro.phonestore.models;

import com.ciro.phonestore.exceptions.InvalidJobNumberException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
public class Job {

    private static final java.util.regex.Pattern JOB_NUMBER_PATTERN = java.util.regex.Pattern.compile("^[Jj]\\d+$");

    @Id
    @Column(length = 100, name = "job_number")
    @NotBlank(message = "Job number is required")
    @Pattern(regexp = "^[Jj]\\d+$", message = "Job number must start with 'J' or 'j' followed by numbers")
    private String jobNumber;

    @NotNull(message = "Job status is required")
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime queueDate;

    private LocalDateTime processingDate;

    private LocalDateTime doneDate;

    public String getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(String jobNumber) {
        if (jobNumber != null && !JOB_NUMBER_PATTERN.matcher(jobNumber).matches()) {
            throw new InvalidJobNumberException(jobNumber);
        }
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