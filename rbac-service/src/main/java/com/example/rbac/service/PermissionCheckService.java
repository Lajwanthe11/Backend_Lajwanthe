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

        public Set<String> getResolvedPermissions(
                        String userId,
                        String tenantId) {

                return permissionResolver.resolvePermissions(
                                userId,
                                tenantId);
        }

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