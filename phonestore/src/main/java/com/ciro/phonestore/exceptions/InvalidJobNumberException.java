package com.ciro.phonestore.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidJobNumberException extends RuntimeException {
    public InvalidJobNumberException(String message) {
        super(message);
    }
}