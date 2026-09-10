package com.example.rbac.service;

import java.util.UUID;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(UUID permissionId) {
        super("Permission not found: " + permissionId);
    }
}
