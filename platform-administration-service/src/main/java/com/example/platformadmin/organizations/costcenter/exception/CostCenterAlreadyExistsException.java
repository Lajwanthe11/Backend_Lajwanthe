package com.example.platformadmin.organizations.costcenter.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a Cost Center with the specified
 * business code already exists.
 *
 * <p>This exception results in an HTTP 409 Conflict response.</p>
 */
public class CostCenterAlreadyExistsException extends AppException {

    public CostCenterAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}