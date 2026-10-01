package com.example.platformadmin.organizations.costcenter.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CostCenterNotFoundException extends AppException {

    public CostCenterNotFoundException(UUID id) {
        super(String.format("Cost Center not found with id: '%s'", id), HttpStatus.NOT_FOUND);
    }

    public CostCenterNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}