package com.enterprise.superadmin.license_management_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LicenseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            LicenseNotFoundException exception
    ) {

        return build(
                HttpStatus.NOT_FOUND,
                "LICENSE_NOT_FOUND",
                exception.getMessage()
        );
    }

    @ExceptionHandler(LicenseAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyAssigned(
            LicenseAlreadyAssignedException exception
    ) {

        return build(
                HttpStatus.CONFLICT,
                "LICENSE_ALREADY_ASSIGNED",
                exception.getMessage()
        );
    }

    @ExceptionHandler(LicenseExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(
            LicenseExpiredException exception
    ) {

        return build(
                HttpStatus.CONFLICT,
                "LICENSE_EXPIRED",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidLicenseStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(
            InvalidLicenseStateException exception
    ) {

        return build(
                HttpStatus.CONFLICT,
                "INVALID_LICENSE_STATE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                message
        );
    }

    /**
     * Handles invalid UUID/path/query/header parameters.
     *
     * Example:
     * /licenses/invalid-uuid
     * X-Actor-Id: invalid-uuid
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {

        String message =
                exception.getName()
                        + ": invalid value '"
                        + exception.getValue()
                        + "'";

        return build(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                message
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception exception
    ) {

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred"
        );
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String code,
            String message
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        code,
                        message
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}