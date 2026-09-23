package com.example.platformadmin.organizations.company.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a company operation conflicts with existing data, e.g. duplicate code (HTTP 409 Conflict).
 */
public class CompanyConflictException extends AppException {

    public CompanyConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}