package com.example.platformadmin.superadmin.platform_branding_service.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard API error response.
 *
 * This DTO keeps error responses consistent across the
 * Platform Branding APIs.
 */
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String errorCode;
    private String message;
    private String path;
    private String correlationId;
    private List<FieldErrorResponse> fieldErrors;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String errorCode,
            String message,
            String path,
            String correlationId,
            List<FieldErrorResponse> fieldErrors
    ) {
        this.timestamp = timestamp;
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
        this.fieldErrors = fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public List<FieldErrorResponse> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldErrorResponse> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}