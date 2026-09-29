package com.example.platformadmin.rbac.exception;

import com.example.platformadmin.rbac.entity.Permission;

import java.util.UUID;

public class PermissionGroupNotFoundException extends RuntimeException {

    public PermissionGroupNotFoundException(UUID groupId) {
        super("Permission group not found: " + groupId);
    }
}
