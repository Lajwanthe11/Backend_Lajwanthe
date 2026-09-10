package com.enterprise.superadmin.license_management_service.exception;


public class InvalidLicenseStateException
        extends RuntimeException {

    public InvalidLicenseStateException(
            String message
    ) {
        super(message);
    }
}