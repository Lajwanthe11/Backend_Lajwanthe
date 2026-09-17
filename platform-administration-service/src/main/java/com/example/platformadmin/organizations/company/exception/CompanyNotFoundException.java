package com.example.platformadmin.organizations.company.exception;

import com.example.common.exception.AppException;

import org.springframework.http.HttpStatus;

/**

 * Thrown when a company cannot be found by ID or code (HTTP 404 Not Found).

 */

public class CompanyNotFoundException extends AppException {

    public CompanyNotFoundException(Long id) {

        super(String.format("Company not found with id: '%s'", id), HttpStatus.NOT_FOUND);

    }

    public CompanyNotFoundException(String message) {

        super(message, HttpStatus.NOT_FOUND);

    }

}
