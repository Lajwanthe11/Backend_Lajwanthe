package com.example.platformadmin.superadmin.feature_management_service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class FeatureGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(FeatureGlobalExceptionHandler.class);

    @ExceptionHandler(FeatureNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            FeatureNotFoundException exception) {
        log.warn("FeatureNotFoundException caught: {}", exception.getMessage());
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidFeatureStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidState(
            InvalidFeatureStateException exception) {
        log.warn("InvalidFeatureStateException caught: {}", exception.getMessage());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(FeatureConfigurationException.class)
    public ResponseEntity<Map<String, Object>> handleConfiguration(
            FeatureConfigurationException exception) {
        log.warn("FeatureConfigurationException caught: {}", exception.getMessage());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {
        log.warn("IllegalArgumentException caught: {}", exception.getMessage());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {
        String msg = String.format("Invalid parameter '%s': '%s'. Expected a valid UUID (e.g. 123e4567-e89b-12d3-a456-426614174000)",
                exception.getName(), exception.getValue());
        log.warn("MethodArgumentTypeMismatchException caught: {}", msg);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                msg
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {
        log.warn("MethodArgumentNotValidException caught: {}", exception.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", 400);
        response.put("error", "Validation Error");

        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        response.put("messages", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception exception) {
        log.error("Unhandled Exception caught: {}", exception.getMessage(), exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage() != null ? exception.getMessage() : "Internal Server Error"
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);

        return ResponseEntity
                .status(status)
                .body(response);
    }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
                HttpMessageNotReadableException ex) {

            Map<String, Object> response = new HashMap<>();

            Throwable cause = ex.getCause();

            if (cause instanceof InvalidFormatException invalidFormatException
                    && invalidFormatException.getTargetType() != null
                    && invalidFormatException.getTargetType().isEnum()) {

                Class<?> enumClass = invalidFormatException.getTargetType();

                String allowedValues = Arrays.stream(enumClass.getEnumConstants())
                        .map(Object::toString)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");

                response.put("error", "Bad Request");
                response.put(
                        "message",
                        "Invalid value '" + invalidFormatException.getValue()
                                + "' for field. Allowed values are: "
                                + allowedValues
                );
                response.put("status", 400);

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(response);
            }

            response.put("error", "Bad Request");
            response.put("message", "Invalid request body");
            response.put("status", 400);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

}



