package com.enterprise.superadmin.platform_health_service.exception;

public class HealthServiceException extends RuntimeException {
    public HealthServiceException(String message) {
        super(message);
    }

    public HealthServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}