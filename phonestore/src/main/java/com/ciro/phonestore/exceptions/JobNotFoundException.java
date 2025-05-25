package com.ciro.phonestore.exceptions;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String jobNumber) {
        super("Job not found with number: " + jobNumber + "\n" +
                "Please check the job number and try again.");
    }
}