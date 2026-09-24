package com.example.platformadmin.organizations.organization.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class OrganizationNotFoundException extends AppException {

    public OrganizationNotFoundException(UUID id) {
        super(String.format("Organization not found with id: '%s'", id), HttpStatus.NOT_FOUND);
    }

    public OrganizationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}