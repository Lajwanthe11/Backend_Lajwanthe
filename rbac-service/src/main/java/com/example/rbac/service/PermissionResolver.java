package com.example.rbac.service;

import java.util.Set;

public interface PermissionResolver {

    Set<String> resolvePermissions(
            String userId,
            String tenantId
    );

    default boolean hasPermission(
            String userId,
            String permissionCode) {

        return resolvePermissions(userId, "").contains(permissionCode);
    }
}