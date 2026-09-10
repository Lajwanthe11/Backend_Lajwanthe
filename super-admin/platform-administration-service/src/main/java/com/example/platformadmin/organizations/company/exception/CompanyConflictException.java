package com.example.platformadmin.organizations.company.exception;

/**
 * Thrown when a company operation conflicts with existing data (e.g. duplicate code).
 */
public class CompanyConflictException extends RuntimeException {

    public CompanyConflictException(String message) {
        super(message);
    }
}
