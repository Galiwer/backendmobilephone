package com.ciro.phonestore.exceptions;

public class InvalidJobNumberFormatException extends RuntimeException {
    public InvalidJobNumberFormatException(String message) {
        super(message);
    }
}