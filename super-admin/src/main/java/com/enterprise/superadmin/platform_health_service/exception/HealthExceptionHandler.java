package com.enterprise.superadmin.platform_health_service.exception;

import com.enterprise.superadmin.platform_health_service.dto.response.ErrorResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@RestControllerAdvice
public class HealthExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(HealthExceptionHandler.class);

    @ExceptionHandler(HealthSourceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleHealthSourceUnavailable(
            HealthSourceUnavailableException exception) {

        ErrorResponse errorResponse = new ErrorResponse(
                "HEALTH_SOURCE_UNAVAILABLE",
                exception.getMessage(),
                Instant.now(),
                MDC.get("X-Correlation-ID")
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception) {
        log.error(
                "Unexpected error occurred. Correlation ID: {}",
                MDC.get("X-Correlation-ID"),
                exception
        );

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                Instant.now(),
                MDC.get("X-Correlation-ID")
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}