package com.example.platformadmin.superadmin.platform_branding_service.exception;

/**
 * Raised when platform branding already exists and a create/initialize
 * operation cannot be performed again.
 */
public class BrandingAlreadyExistsException extends RuntimeException {

    public BrandingAlreadyExistsException(String message) {
        super(message);
    }
}