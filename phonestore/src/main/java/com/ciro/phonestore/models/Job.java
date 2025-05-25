package com.ciro.phonestore.models;

import com.ciro.phonestore.exceptions.InvalidJobNumberException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Entity
public class Job {

    private static final Pattern JOB_NUMBER_PATTERN = Pattern.compile("^[Jj]\\d+$");

    @Id
    @Column(length = 100, name = "job_number")
    private String jobNumber;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime queueDate;

    private LocalDateTime processingDate;

    private LocalDateTime doneDate;

    private String deviceModel;

    private String description;

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

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}