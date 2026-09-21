package com.example.platformadmin.organizations.businessunit.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a business unit cannot be found by ID or code (HTTP 404 Not Found).
 */
public class BusinessUnitNotFoundException extends AppException {

    public BusinessUnitNotFoundException(Long id) {
        super(String.format("BusinessUnit not found with id: '%s'", id), HttpStatus.NOT_FOUND);
    }

    public BusinessUnitNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}