package com.example.platformadmin.rbac.exception;

import com.example.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class DataPermissionEvaluationException extends AppException {

    public DataPermissionEvaluationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DataPermissionEvaluationException(String message, HttpStatus status) {
        super(message, status);
    }
}
