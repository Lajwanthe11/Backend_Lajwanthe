package com.example.platformadmin.superadmin.globaldashboard.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice
public class GlobalDashboardExceptionHandler {

    @ExceptionHandler(GlobalDashboardValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            GlobalDashboardValidationException ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
                "VALIDATION_ERROR",
                ex.getMessage(),
                Instant.now(),
                UUID.randomUUID().toString(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred.",
                Instant.now(),
                UUID.randomUUID().toString(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}