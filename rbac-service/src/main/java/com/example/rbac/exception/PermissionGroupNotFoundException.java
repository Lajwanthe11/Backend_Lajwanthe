package com.example.rbac.exception;

import java.util.UUID;

public class PermissionGroupNotFoundException extends RuntimeException {

    public PermissionGroupNotFoundException(UUID groupId) {
        super("Permission group not found: " + groupId);
    }
}
