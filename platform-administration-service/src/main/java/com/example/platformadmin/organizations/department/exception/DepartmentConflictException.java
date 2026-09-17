package com.example.platformadmin.organizations.department.exception;

import com.example.common.exception.AppException;

import org.springframework.http.HttpStatus;

/**

 * Thrown when a department code already exists (HTTP 409 Conflict).

 */

public class DepartmentConflictException extends AppException {

    public DepartmentConflictException(String message) {

        super(message, HttpStatus.CONFLICT);

    }

}
