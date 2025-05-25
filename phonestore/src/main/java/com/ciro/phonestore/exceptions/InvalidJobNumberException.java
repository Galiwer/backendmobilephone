package com.ciro.phonestore.exceptions;

public class InvalidJobNumberException extends RuntimeException {
    public InvalidJobNumberException(String jobNumber) {
        super("Invalid job number format: " + jobNumber
                + ". Job number must be in format 'J' followed by a number (e.g., J1, J2, J3)");
    }
}