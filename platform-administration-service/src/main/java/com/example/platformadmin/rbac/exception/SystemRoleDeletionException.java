package com.example.platformadmin.rbac.exception;

import com.example.common.exception.BadRequestException;

public class SystemRoleDeletionException extends BadRequestException {
    public SystemRoleDeletionException(String message) {
        super(message);
    }
}
