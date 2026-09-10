package com.enterprise.superadmin.license_management_service.exception;


public class LicenseExpiredException
        extends RuntimeException {

    public LicenseExpiredException(
            String message
    ) {
        super(message);
    }
}
