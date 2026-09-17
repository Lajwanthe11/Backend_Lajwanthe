package com.example.platformadmin.superadmin.platformconfiguration.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.platformadmin.superadmin.platformconfiguration.controller.PlatformConfigurationController;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global centralized REST exception handler intercepting framework and business exceptions.
 *
 * <p>Translates all errors into standardized RFC 7807 / FRS-compliant {@link ApiError} payloads
 * containing HTTP status code, textual phrase, FRS error code (e.g., ERR-0007, ERR-0008, ERR-0009,
 * ERR-0011, ERR-0012), sanitized error message, request path, and field-level validation maps.</p>
 */
@RestControllerAdvice(assignableTypes = PlatformConfigurationController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PlatformConfigurationGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PlatformConfigurationGlobalExceptionHandler.class);

    /**
     * Handles resource not found scenarios (FRS ERR-0011).
     *
     * @param exception the thrown not-found exception
     * @param request   the executing servlet request
     * @return 404 NOT_FOUND response with ERR-0011 code
     */
    @ExceptionHandler(PlatformConfigurationNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(PlatformConfigurationNotFoundException exception,
                                                   HttpServletRequest request) {
        log.warn("Resource not found: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "ERR-0011", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles duplicate configuration name conflict (ERR-0007, BR-0012).
     */
    @ExceptionHandler(DuplicateConfigurationNameException.class)
    public ResponseEntity<ApiError> handleDuplicateConfiguration(DuplicateConfigurationNameException exception,
                                                                 HttpServletRequest request) {
        log.warn("Conflict detected: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "ERR-0007", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles database unique constraint violations arising from concurrent requests (ERR-0007).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException exception,
                                                                HttpServletRequest request) {
        log.warn("Database constraint violation on {} {}: {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        String msg = "A configuration with this name already exists or violates database integrity constraints.";
        return buildResponse(HttpStatus.CONFLICT, "ERR-0007", msg, request.getRequestURI(), Map.of());
    }

    /**
     * Handles invalid configuration values (ERR-0008, VAL-0008, VAL-0010).
     */
    @ExceptionHandler(InvalidConfigurationValueException.class)
    public ResponseEntity<ApiError> handleInvalidConfigurationValue(InvalidConfigurationValueException exception,
                                                                    HttpServletRequest request) {
        log.warn("Invalid configuration value: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0008", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles pre-activation validation failure (ERR-0009, VAL-0012, BR-0020).
     */
    @ExceptionHandler(ConfigurationActivationException.class)
    public ResponseEntity<ApiError> handleConfigurationActivation(ConfigurationActivationException exception,
                                                                  HttpServletRequest request) {
        log.warn("Configuration activation failure: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0009", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles configuration import failure (ERR-0010).
     */
    @ExceptionHandler(ConfigurationImportException.class)
    public ResponseEntity<ApiError> handleConfigurationImport(ConfigurationImportException exception,
                                                              HttpServletRequest request) {
        log.warn("Configuration import failure: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0010", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles security authorization failure (ERR-0006).
     */
    @ExceptionHandler(UnauthorizedConfigurationAccessException.class)
    public ResponseEntity<ApiError> handleUnauthorizedAccess(UnauthorizedConfigurationAccessException exception,
                                                             HttpServletRequest request) {
        log.warn("Unauthorized access attempt: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "ERR-0006", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles database/infrastructure connectivity failures (ERR-0012).
     */
    @ExceptionHandler(ConfigurationRepositoryUnavailableException.class)
    public ResponseEntity<ApiError> handleRepositoryUnavailable(ConfigurationRepositoryUnavailableException exception,
                                                                HttpServletRequest request) {
        log.error("Configuration repository unavailable: {} {}", request.getMethod(), request.getRequestURI(),
                exception);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "ERR-0012", exception.getMessage(), request.getRequestURI(), Map.of());
    }

    /**
     * Handles request body bean validation failures (ERR-0008).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
                                                     HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation error on request: {} {} - errors: {}", request.getMethod(), request.getRequestURI(),
                validationErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0008", "Request validation failed", request.getRequestURI(),
                validationErrors);
    }

    /**
     * Handles unreadable or malformed JSON payloads (ERR-0008).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest(HttpMessageNotReadableException exception,
                                                            HttpServletRequest request) {
        String message = "Invalid request body.";
        if (exception.getCause() instanceof InvalidFormatException ife && ife.getTargetType() != null
                && ife.getTargetType().isEnum()) {
            message = "Invalid value for " + resolveFieldName(ife) + ".";
        }

        log.warn("Malformed HTTP request body: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0008", message, request.getRequestURI(), Map.of());
    }

    /**
     * Handles unmapped endpoints (ERR-0011).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(NoResourceFoundException exception,
                                                           HttpServletRequest request) {
        log.warn("Unmapped endpoint requested: {} {}", request.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, "ERR-0011", "Resource not found", request.getRequestURI(), Map.of());
    }

    /**
     * Handles Spring Security access denied (ERR-0006).
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(org.springframework.security.access.AccessDeniedException exception,
                                                       HttpServletRequest request) {
        log.warn("Access denied: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "ERR-0006", "Access denied: insufficient permissions", request.getRequestURI(), Map.of());
    }

    /**
     * Fallback handler for unexpected internal server errors (ERR-0001).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled internal server error: {} {} - {}", request.getMethod(), request.getRequestURI(),
                exception.getMessage(), exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERR-0001", "An unexpected error occurred", request.getRequestURI(),
                Map.of());
    }
    
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        String paramName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid format";
        String message = String.format("Parameter '%s' has an invalid format. Expected type: %s", paramName, requiredType);

        log.warn("Path/Query parameter type mismatch: {} - {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0008", message, request.getRequestURI(), Map.of());
    }
   
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String property = violation.getPropertyPath().toString();
            errors.put(property, violation.getMessage());
        });
        return buildResponse(HttpStatus.BAD_REQUEST, "ERR-0008", "Parameter validation failed", request.getRequestURI(), errors);
    }
    
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {
        String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "ERR-0008", message, request.getRequestURI(), Map.of());
    }
    
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(
            org.springframework.web.HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {
        String message = String.format("Media type '%s' is not supported. Please use 'application/json'.", ex.getContentType());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "ERR-0008", message, request.getRequestURI(), Map.of());
    }

    private String resolveFieldName(InvalidFormatException exception) {
        if (exception.getPath() != null && !exception.getPath().isEmpty()
                && exception.getPath().get(0).getFieldName() != null) {
            return exception.getPath().get(0).getFieldName();
        }
        return "request field";
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String errorCode, String message, String path,
                                                   Map<String, String> validationErrors) {
        ApiError error = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), errorCode, message, path,
                validationErrors);
        return ResponseEntity.status(status).body(error);
    }
}