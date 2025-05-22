package com.ciro.phonestore.models;

public enum JobStatus {
    IN_QUEUE(1, "In Queue"),
    IN_PROGRESS(2, "In Progress"),
    COMPLETED(3, "Completed");

    private final int code;
    private final String description;

    JobStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static JobStatus fromCode(int code) {
        for (JobStatus status : JobStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid job status code: " + code);
    }
}