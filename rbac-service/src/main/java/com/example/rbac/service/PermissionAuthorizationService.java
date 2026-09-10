package com.example.rbac.service;

import com.example.rbac.config.RequirePermission;
import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class PermissionAuthorizationService {

    private final PermissionResolver permissionResolver;
    private final SecurityContextUtil securityContextUtil;

    public PermissionAuthorizationService(PermissionResolver permissionResolver,
            SecurityContextUtil securityContextUtil) {
        this.permissionResolver = permissionResolver;
        this.securityContextUtil = securityContextUtil;
    }

    /**
     * Throws {@link PermissionDeniedException} if the current authenticated
     * user does not satisfy the given {@code @RequirePermission} rule.
     * Returns normally if access is allowed.
     */
    public void authorize(RequirePermission requirePermission) {
        AuthenticatedUser user = securityContextUtil.currentUser();

        Set<String> requireAll = merge(requirePermission.value(), requirePermission.requireAll());
        Set<String> requireAny = Set.of(requirePermission.requireAny());

        if (requireAll.isEmpty() && requireAny.isEmpty()) {
            // Annotation present but empty - fail closed rather than silently allow.
            throw new PermissionDeniedException(
                    "No permission codes configured on @RequirePermission", "UNSPECIFIED");
        }

        for (String permissionCode : requireAll) {
            if (!permissionResolver.hasPermission(user.userId(), permissionCode)) {
                throw new PermissionDeniedException("Missing required permission", permissionCode);
            }
        }

        if (!requireAny.isEmpty()) {
            boolean hasAtLeastOne = requireAny.stream()
                    .anyMatch(code -> permissionResolver.hasPermission(user.userId(), code));
            if (!hasAtLeastOne) {
                throw new PermissionDeniedException(
                        "None of the required (any-of) permissions were present",
                        String.join(",", requireAny));
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
