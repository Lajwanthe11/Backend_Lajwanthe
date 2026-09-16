package com.example.platformadmin.superadmin.platform_branding_service.exception;

/**
 * Exception thrown when branding validation fails due to invalid input.
 */
public class InvalidBrandingException extends RuntimeException {
    public InvalidBrandingException(String message) {
        super(message);
    }
}