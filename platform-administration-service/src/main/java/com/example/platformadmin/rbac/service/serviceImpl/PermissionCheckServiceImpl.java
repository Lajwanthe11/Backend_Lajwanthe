package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.entity.Permission;

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
public class PermissionCheckServiceImpl implements PermissionCheckService {

    private static final Logger log =
            LoggerFactory.getLogger(PermissionCheckServiceImpl.class);

    private final PermissionResolver permissionResolver;

    public PermissionCheckServiceImpl(
            PermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    // Checks if the user has the given permission in the tenant.
    @Override
    public boolean hasPermission(
            String userId,
            String tenantId,
            String permissionCode) {

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

    // Gets all permissions resolved for the user.
    @Override
    public Set<String> getResolvedPermissions(
            String userId,
            String tenantId) {

        return permissionResolver.resolvePermissions(
                userId,
                tenantId);
    }

    // Checks the permission using the currently logged-in user.
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

        String username = authentication.getName();
        String tenantId = TenantContext.getTenantId();

        return hasPermission(
                username,
                tenantId,
                permissionCode);
    }
}
