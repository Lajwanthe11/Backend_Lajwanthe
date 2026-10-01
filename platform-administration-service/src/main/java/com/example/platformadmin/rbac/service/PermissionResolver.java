package com.example.platformadmin.rbac.service;

import java.util.Set;

public interface PermissionResolver {

    Set<String> resolvePermissions(
            String userId,
            String tenantId);

    boolean hasPermission(
            String userId,
            String permissionCode);
}
