package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.service.PermissionCheckService;
import com.example.platformadmin.rbac.service.PermissionResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PermissionCheckServiceImpl
        implements PermissionCheckService {

    private static final Logger log =
            LoggerFactory.getLogger(PermissionCheckServiceImpl.class);

    private final PermissionResolver permissionResolver;

    public PermissionCheckServiceImpl(
            PermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    // Check whether a user has a permission
    @Override
    public boolean hasPermission(
            String userId,
            String permissionCode) {

        String tenantId = TenantContext.getTenantId();

        Set<String> permissions =
                permissionResolver.resolvePermissions(
                        userId,
                        tenantId);

        boolean allowed =
                permissions.contains("*")
                        || permissions.contains(permissionCode);

        if (!allowed) {
            log.warn(
                    "Permission denied: userId={}, tenantId={}, permissionCode={}",
                    userId,
                    tenantId,
                    permissionCode);
        }

        return allowed;
    }

    // Get all permissions for a user
    @Override
    public Set<String> getResolvedPermissions(
            String userId,
            String tenantId) {

        return permissionResolver.resolvePermissions(
                userId,
                tenantId);
    }

    // Check permission for the current user
    @Override
    public boolean hasPermission(String permissionCode) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        String userId = authentication.getName();

        return hasPermission(
                userId,
                permissionCode);
    }
}
