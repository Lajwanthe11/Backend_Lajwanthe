package com.example.rbac.service;

import java.util.Set;

public interface PermissionResolver {

    Set<String> resolvePermissions(
            String userId,
            String tenantId
    );

    /**
     * Returns {@code true} if the user identified by {@code userId} holds
     * the given {@code permissionCode}.
     *
     * <p>The default implementation resolves all permissions for the user
     * (passing an empty tenantId – override this method in implementations
     * that need tenant-scoped resolution) and checks for membership.
     */
    default boolean hasPermission(String userId, String permissionCode) {
        return resolvePermissions(userId, "").contains(permissionCode);
    }
}