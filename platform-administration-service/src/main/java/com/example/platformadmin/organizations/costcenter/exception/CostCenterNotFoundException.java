package com.example.platformadmin.organizations.costcenter.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class CostCenterNotFoundException extends AppException {

    public CostCenterNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}