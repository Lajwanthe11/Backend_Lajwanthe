package com.example.platformadmin.superadmin.platformconfiguration.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized API Error Response Payload.
 *
 * <p>Complies with FRS error specifications including machine-readable
 * {@code errorCode} (e.g. ERR-0007, ERR-0008, ERR-0009, ERR-0011, ERR-0012).</p>
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    /**
     * Backward-compatible constructor defaulting errorCode to "ERR-GENERIC".
     */
    public ApiError(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors) {
        this(timestamp, status, error, "ERR-GENERIC", message, path, validationErrors);
    }
}