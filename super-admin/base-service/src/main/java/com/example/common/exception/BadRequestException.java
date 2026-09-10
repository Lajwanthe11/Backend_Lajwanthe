package com.example.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for 400 Bad Request scenarios (e.g., duplicate username, invalid input).
 */
public class BadRequestException extends AppException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
