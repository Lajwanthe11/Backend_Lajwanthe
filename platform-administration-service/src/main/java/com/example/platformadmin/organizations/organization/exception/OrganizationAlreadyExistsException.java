package com.example.platformadmin.organizations.organization.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class OrganizationAlreadyExistsException extends AppException {

    public OrganizationAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}