package com.example.rbac.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.common.tenant.TenantContext;
import org.springframework.security.core.Authentication;
import java.util.Set;

@Service
public class PermissionCheckService {

        private static final Logger log = LoggerFactory.getLogger(PermissionCheckService.class);

        private final PermissionResolver permissionResolver;

        public PermissionCheckService(PermissionResolver permissionResolver) {
                this.permissionResolver = permissionResolver;
        }

        // Checks if the user has the given permission in the tenant.
        public boolean hasPermission(
                        String userId,
                        String tenantId,
                        String permissionCode) {

                Set<String> permissions = permissionResolver.resolvePermissions(
                                userId,
                                tenantId);

                boolean allowed = permissions.contains("*")
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
        public Set<String> getResolvedPermissions(
                        String userId,
                        String tenantId) {

                return permissionResolver.resolvePermissions(
                                userId,
                                tenantId);
        }

        // Checks the permission using the currently logged-in user.
        public boolean hasPermission(String permissionCode) {

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication == null || !authentication.isAuthenticated()) {
                        return false;
                }

                String username = authentication.getName();
                String tenantId = TenantContext.getTenantId();

                return hasPermission(username, tenantId, permissionCode);
        }
}