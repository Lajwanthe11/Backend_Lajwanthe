package com.enterprise.superadmin.license_management_service.exception;


public class LicenseAlreadyAssignedException
        extends RuntimeException {

    public LicenseAlreadyAssignedException(
            String message
    ) {
        super(message);
    }
}