package com.example.platformadmin.organizations.businessunit.exception;

import com.example.common.exception.AppException;

import org.springframework.http.HttpStatus;

/**

 * Thrown when a business unit code already exists (HTTP 409 Conflict).

 */

public class BusinessUnitConflictException extends AppException {

    public BusinessUnitConflictException(String message) {

        super(message, HttpStatus.CONFLICT);

    }

}
