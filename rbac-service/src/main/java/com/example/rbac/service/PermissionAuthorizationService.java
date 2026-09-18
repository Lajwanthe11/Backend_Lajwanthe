package com.example.rbac.service;

import com.example.rbac.config.RequirePermission;
import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.AuthenticatedUser;
import org.springframework.stereotype.Service;
import com.example.rbac.exception.PermissionDeniedException;

import java.util.LinkedHashSet;
import java.util.Set;

// This is the core decision-maker. It gets the current user, resolves permissions, merges required
//permissions, handles requireAll/requireAny, supports * as a super-admin wildcard, and fails closed.
@Service
public class PermissionAuthorizationService {

    private final PermissionResolver permissionResolver;
    private final SecurityContextUtil securityContextUtil;

    public PermissionAuthorizationService(PermissionResolver permissionResolver,
            SecurityContextUtil securityContextUtil) {
        this.permissionResolver = permissionResolver;
        this.securityContextUtil = securityContextUtil;
    }

    public void authorize(RequirePermission requirePermission) {
        AuthenticatedUser user = securityContextUtil.currentUser();

        String tenantId = user.tenantId() != null ? user.tenantId() : "";
        Set<String> rawGranted = permissionResolver.resolvePermissions(user.userId(), tenantId);
        final Set<String> granted = (rawGranted != null) ? rawGranted : Set.of();
        boolean isSuperAdmin = granted.contains("*") || permissionResolver.hasPermission(user.userId(), "*");

        Set<String> requireAll = merge(requirePermission.value(), requirePermission.requireAll());
        Set<String> requireAny = Set.of(requirePermission.requireAny());

        if (requireAll.isEmpty() && requireAny.isEmpty()) {

            // Annotation present but empty - fail closed rather than silently allow.

            throw new PermissionDeniedException(
                    "No permission codes configured on @RequirePermission", "UNSPECIFIED");
        }

        if (!isSuperAdmin) {
            for (String permissionCode : requireAll) {
                if (!granted.contains(permissionCode) && !permissionResolver.hasPermission(user.userId(), permissionCode)) {
                    throw new PermissionDeniedException(
                            "Missing required permission", permissionCode);
                }
            }

            if (!requireAny.isEmpty()) {
                boolean hasAtLeastOne = requireAny.stream()
                        .anyMatch(p -> granted.contains(p) || permissionResolver.hasPermission(user.userId(), p));
                if (!hasAtLeastOne) {
                    throw new PermissionDeniedException(
                            "None of the required (any-of) permissions were present",
                            String.join(",", requireAny));
                }
            }
        }
    }

    private Set<String> merge(String[] value, String[] requireAll) {
        Set<String> merged = new LinkedHashSet<>();
        merged.addAll(Set.of(value));
        merged.addAll(Set.of(requireAll));
        return merged;
    }
}
