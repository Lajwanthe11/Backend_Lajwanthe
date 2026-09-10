package com.example.rbac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionCheckService {

    private static final Logger log =
            LoggerFactory.getLogger(PermissionCheckService.class);

    private final PermissionResolver permissionResolver;

    public PermissionCheckService(PermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    public boolean hasPermission(
            String userId,
            String tenantId,
            String permissionCode) {

        Set<String> permissions =
                permissionResolver.resolvePermissions(
                        userId,
                        tenantId
                );

        boolean allowed =
                permissions.contains("*")
                        || permissions.contains(permissionCode);

        if (!allowed) {
            log.warn(
                    "Permission denied: userId={}, tenantId={}, permissionCode={}",
                    userId,
                    tenantId,
                    permissionCode
            );
        }

        return allowed;
    }

    public Set<String> getResolvedPermissions(
            String userId,
            String tenantId) {

        return permissionResolver.resolvePermissions(
                userId,
                tenantId
        );
    }
}