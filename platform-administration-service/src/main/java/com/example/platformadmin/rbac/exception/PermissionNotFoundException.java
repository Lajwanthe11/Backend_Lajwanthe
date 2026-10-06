package com.example.platformadmin.rbac.exception;

import com.example.platformadmin.rbac.entity.Permission;

import java.util.UUID;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(UUID permissionId) {
        super("Permission not found: " + permissionId);
    }
}
