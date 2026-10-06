package com.example.platformadmin.organizations.costcenter.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class CostCenterAlreadyExistsException extends AppException {

    public CostCenterAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}