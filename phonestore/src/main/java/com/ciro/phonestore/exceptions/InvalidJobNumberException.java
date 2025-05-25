package com.ciro.phonestore.exceptions;

public class InvalidJobNumberException extends RuntimeException {
    public InvalidJobNumberException(String jobNumber) {
        super("Invalid job number format: " + jobNumber
                + ". Job number must start with 'J' or 'j' followed by any number (e.g., J1, j2, J42, j100)");
    }
}