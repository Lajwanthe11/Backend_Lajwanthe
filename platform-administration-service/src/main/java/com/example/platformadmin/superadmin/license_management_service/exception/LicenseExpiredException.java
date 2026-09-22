package com.example.platformadmin.superadmin.license_management_service.exception;


public class LicenseExpiredException
        extends RuntimeException {

    public LicenseExpiredException(
            String message
    ) {
        super(message);
    }
}
