package com.example.platformadmin.rbac.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidDataAccessRuleException extends AppException {

    public InvalidDataAccessRuleException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
