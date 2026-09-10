package com.enterprise.superadmin.platform_branding_service.exception;

import com.enterprise.superadmin.platform_branding_service.dto.response.ApiErrorResponse;
import com.enterprise.superadmin.platform_branding_service.dto.response.FieldErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Centralized exception handler for Platform Branding APIs.
 *
 * Responsibilities:
 * - Convert application exceptions into consistent API responses.
 * - Handle DTO validation failures.
 * - Prevent internal exception details from reaching clients.
 * - Provide a correlation identifier for troubleshooting.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------------------
    // Branding not found
    // ---------------------------------------------------------------------

    @ExceptionHandler(BrandingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBrandingNotFound(BrandingNotFoundException exception, HttpServletRequest request) {

        String correlationId = generateCorrelationId();

        log.warn("Branding resource not found. correlationId={}, path={}", correlationId, request.getRequestURI());

        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "BRANDING_NOT_FOUND",
                exception.getMessage(),
                request,
                correlationId,
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ---------------------------------------------------------------------
    // Branding already exists
    // ---------------------------------------------------------------------

    @ExceptionHandler(BrandingAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleBrandingAlreadyExists(BrandingAlreadyExistsException exception, HttpServletRequest request) {

        String correlationId = generateCorrelationId();

        log.warn("Branding creation conflict. correlationId={}, path={}", correlationId, request.getRequestURI());

        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.CONFLICT,
                "BRANDING_ALREADY_EXISTS",
                exception.getMessage(),
                request,
                correlationId,
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ---------------------------------------------------------------------
    // DTO validation
    // ---------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {

        String correlationId = generateCorrelationId();

        List<FieldErrorResponse> fieldErrors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(fieldError ->
                                new FieldErrorResponse(
                                        fieldError.getField(),
                                        fieldError.getDefaultMessage()
                                )
                        )
                        .toList();

        log.warn("Branding request validation failed. " + "correlationId={}, path={}, fieldErrorCount={}", correlationId, request.getRequestURI(), fieldErrors.size());

        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Request validation failed",
                request,
                correlationId,
                fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }



    // ---------------------------------------------------------------------
    // Illegal argument / business input error
    // ---------------------------------------------------------------------

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {

        String correlationId = generateCorrelationId();

        log.warn("Invalid branding request. correlationId={}, path={}", correlationId, request.getRequestURI());

        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                exception.getMessage(),
                request,
                correlationId,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

// ---------------------------------------------------------------------
// Branding invalid state
// ---------------------------------------------------------------------

    @ExceptionHandler(BrandingInvalidStateException.class)
    public ResponseEntity<ApiErrorResponse> handleBrandingInvalidState(BrandingInvalidStateException exception, HttpServletRequest request) {

        String correlationId = generateCorrelationId();

        log.warn("Invalid branding state. correlationId={}, path={}", correlationId, request.getRequestURI());

        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.CONFLICT,
                "BRANDING_INVALID_STATE",
                exception.getMessage(),
                request,
                correlationId,
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ---------------------------------------------------------------------
    // Unexpected exception
    // ---------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {

        String correlationId = generateCorrelationId();

        /*
         * Log the actual exception internally.
         * Do NOT return the exception message/stack trace to the client.
         */
        log.error("Unexpected error while processing branding request. " + "correlationId={}, path={}", correlationId, request.getRequestURI(), exception);

        ApiErrorResponse response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request,
                correlationId,
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ---------------------------------------------------------------------
    // Response builder
    // ---------------------------------------------------------------------

    private ApiErrorResponse buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            String correlationId,
            List<FieldErrorResponse> fieldErrors
    ) {

        return new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                correlationId,
                fieldErrors
        );
    }

    // ---------------------------------------------------------------------
    // Correlation ID
    // ---------------------------------------------------------------------

    private String generateCorrelationId() {

        return UUID.randomUUID().toString();
    }
}