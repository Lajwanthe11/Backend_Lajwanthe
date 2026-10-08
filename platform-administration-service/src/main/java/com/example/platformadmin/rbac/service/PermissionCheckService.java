package com.example.platformadmin.rbac.service;

import java.util.Set;

public interface PermissionCheckService {

    // Check whether a user has a permission
    boolean hasPermission(
            String userId,
            String permissionCode);

    // Get all permissions for a user
    Set<String> getResolvedPermissions(
            String userId,
            String tenantId);

    // Check permission for the current user
    boolean hasPermission(String permissionCode);
}
