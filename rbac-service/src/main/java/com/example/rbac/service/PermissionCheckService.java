package com.example.rbac.service;

import java.util.Set;

public interface PermissionCheckService {

    boolean hasPermission(
            String userId,
            String tenantId,
            String permissionCode);

    Set<String> getResolvedPermissions(
            String userId,
            String tenantId);

    boolean hasPermission(String permissionCode);
}