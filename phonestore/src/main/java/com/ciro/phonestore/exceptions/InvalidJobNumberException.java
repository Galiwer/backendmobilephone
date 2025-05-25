package com.ciro.phonestore.exceptions;

public class InvalidJobNumberException extends RuntimeException {
    public InvalidJobNumberException(String jobNumber) {
        super("Error: Invalid job number format '" + jobNumber
                + "'. Job number must start with 'J' or 'j' followed by numbers (Examples: J1, j2, J42)");
    }
}