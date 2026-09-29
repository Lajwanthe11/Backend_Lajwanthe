package com.example.platformadmin.rbac.exception;

import com.example.common.exception.BadRequestException;

public class RoleConflictException extends BadRequestException {
    public RoleConflictException(String message) {
        super(message);
    }
}
