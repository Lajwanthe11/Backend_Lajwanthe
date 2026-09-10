package com.example.rbac.service;

import java.util.Set;

public interface PermissionResolver {

    Set<String> resolvePermissions(
            String userId,
            String tenantId
    );
}