package com.example.platformadmin.superadmin.platform_branding_service.exception;

/**
 * Raised when the requested platform branding configuration cannot be found.
 */
public class BrandingNotFoundException extends RuntimeException {

    public BrandingNotFoundException(String message) {
        super(message);
    }
}