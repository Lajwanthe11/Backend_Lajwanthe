package com.enterprise.superadmin.license_management_service.exception;


public class LicenseNotFoundException
        extends RuntimeException {

    public LicenseNotFoundException(
            String message
    ) {
        super(message);
    }
}