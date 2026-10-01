package com.example.platformadmin.organizations.costcenter.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InvalidOrganizationIdException extends AppException {

    public InvalidOrganizationIdException(UUID organizationId) {
        super("Invalid organization ID: " + organizationId, HttpStatus.BAD_REQUEST);
    }
}
