package com.example.platformadmin.organizations.department.exception;

import com.example.common.exception.AppException;

import org.springframework.http.HttpStatus;

/**

 * Thrown when a department cannot be found by ID or name (HTTP 404 Not Found).

 */

public class DepartmentNotFoundException extends AppException {

    public DepartmentNotFoundException(Long id) {

        super(String.format("Department not found with id: '%s'", id), HttpStatus.NOT_FOUND);

    }

    public DepartmentNotFoundException(String message) {

        super(message, HttpStatus.NOT_FOUND);

    }

}
