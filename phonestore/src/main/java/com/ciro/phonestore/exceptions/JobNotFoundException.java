package com.ciro.phonestore.exceptions;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String jobNumber) {
        super("Error: Job number " + jobNumber + " not found. Please check and try again.");
    }
}