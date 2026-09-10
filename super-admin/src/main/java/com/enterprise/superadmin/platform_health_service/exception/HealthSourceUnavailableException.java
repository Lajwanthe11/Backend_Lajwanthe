package com.enterprise.superadmin.platform_health_service.exception;

public class HealthSourceUnavailableException extends RuntimeException {

    public HealthSourceUnavailableException(String message) {
        super(message);
    }
}