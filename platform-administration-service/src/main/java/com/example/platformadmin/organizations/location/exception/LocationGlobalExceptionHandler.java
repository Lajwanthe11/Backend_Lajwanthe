package com.example.platformadmin.organizations.location.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class LocationGlobalExceptionHandler {

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleLocationNotFound(
            LocationNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(LocationAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleLocationAlreadyExists(
            LocationAlreadyExistsException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }
}