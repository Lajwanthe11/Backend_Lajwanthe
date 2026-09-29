package com.example.platformadmin.rbac.service;

import java.util.Set;

public interface PermissionResolver {

    // Gets all active permissions assigned to the user for the tenant.
    Set<String> resolvePermissions(
            String userId,
            String tenantId);

    // Checks whether the user has the requested permission.
    default boolean hasPermission(
            String userId,
            String permissionCode) {

        return resolvePermissions(userId, "").contains(permissionCode);
    }
}