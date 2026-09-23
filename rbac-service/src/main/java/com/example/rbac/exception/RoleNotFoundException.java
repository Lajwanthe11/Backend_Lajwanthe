package com.example.rbac.exception;

import java.util.UUID;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(UUID message) {
        super(message + "is not found");
    }

    public RoleNotFoundException(String message) {
        super(message + "is not found");
    }
}
