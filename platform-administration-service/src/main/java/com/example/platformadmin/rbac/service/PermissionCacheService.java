package com.example.platformadmin.rbac.service;

import java.util.Set;

public interface PermissionCacheService {

    void clearUserPermissionsCache(
            String userId,
            String tenantId);

    void clearUsersPermissionsCache(
            Set<String> userIds,
            String tenantId);
}