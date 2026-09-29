package com.example.platformadmin.rbac.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class DataAccessRuleNotFoundException extends AppException {

    public DataAccessRuleNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
