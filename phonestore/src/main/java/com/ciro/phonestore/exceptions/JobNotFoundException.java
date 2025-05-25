package com.ciro.phonestore.exceptions;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String jobNumber) {
        super("Job not found with number: " + jobNumber);
    }
}