package com.example.platformadmin.organizations.costcenter.exception;
import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

/**
 * Exception thrown when the requested Cost Center
 * cannot be found.
 *
 * <p>This exception results in an HTTP 404 Not Found response.</p>
 */
public class CostCenterNotFoundException extends AppException {

    public CostCenterNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}