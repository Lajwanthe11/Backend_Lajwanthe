package com.example.auth.securityalerts.controller;

import com.example.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Client errors that the shared GlobalExceptionHandler has no handler for, and would answer with
 * 500: bad JSON, unknown enum values in the body or query, missing parameters, unknown sort fields.
 * Limited to this package so other controllers keep their current behaviour; checked before the
 * global handler because of the order.
 */
@RestControllerAdvice(basePackageClasses = SecurityAlertExceptionHandler.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityAlertExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Request body is missing or is not valid JSON";
        if (ex.getCause() instanceof InvalidFormatException invalid && !invalid.getPath().isEmpty()) {
            String field = invalid.getPath().get(invalid.getPath().size() - 1).getFieldName();
            message = "Invalid value '" + invalid.getValue() + "' for '" + field + "'" + allowedValues(invalid.getTargetType());
        }
        return error(HttpStatus.BAD_REQUEST, message, null, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'" + allowedValues(ex.getRequiredType()),
                null, request);
    }

    /** Query-parameter objects (filters) and @Valid request bodies. */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBinding(BindException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.isBindingFailure()
                    ? "Invalid value '" + fieldError.getRejectedValue() + "'"
                    : fieldError.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors().forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Input validation error", errors, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Required parameter '" + ex.getParameterName() + "' is missing", null, request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleUnknownSortProperty(PropertyReferenceException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Cannot sort by '" + ex.getPropertyName() + "'", null, request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "The record was changed by someone else in the meantime. Refresh it and try again.", null, request);
    }

    private static String allowedValues(Class<?> type) {
        if (type == null || !type.isEnum()) {
            return "";
        }
        return ". Allowed values: " + Arrays.stream(type.getEnumConstants()).map(Object::toString).collect(Collectors.joining(", "));
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String message, Map<String, String> validationErrors,
                                                       HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .validationErrors(validationErrors)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
