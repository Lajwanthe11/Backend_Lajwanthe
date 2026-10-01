package com.example.platformadmin.organizations.costcenter.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidDepartmentIdException extends AppException {

    public InvalidDepartmentIdException(Long departmentId) {
        super("Invalid department ID: " + departmentId, HttpStatus.BAD_REQUEST);
    }
}