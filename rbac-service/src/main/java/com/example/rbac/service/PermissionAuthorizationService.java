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
     *
     * <p>The tenant identity is sourced exclusively from the verified JWT
     * (via {@link SecurityContextUtil#currentUser()}) and never from
     * caller-supplied request parameters.
     */
    public void authorize(RequirePermission requirePermission) {
        AuthenticatedUser user = securityContextUtil.currentUser();

        // Resolve the full permission set once using the tenant from the JWT.
        // An empty/null tenantId falls back to "" which is consistent with
        // how PermissionResolverImpl keys its cache (perms::userId).
        String tenantId = user.tenantId() != null ? user.tenantId() : "";
        Set<String> granted = permissionResolver.resolvePermissions(user.userId(), tenantId);
        boolean isSuperAdmin = granted.contains("*");

        Set<String> requireAll = merge(requirePermission.value(), requirePermission.requireAll());
        Set<String> requireAny = Set.of(requirePermission.requireAny());

        if (requireAll.isEmpty() && requireAny.isEmpty()) {
            // Annotation present but empty - fail closed rather than silently allow.
            throw new PermissionDeniedException(
                    "No permission codes configured on @RequirePermission", "UNSPECIFIED");
        }

        if (!isSuperAdmin) {
            for (String permissionCode : requireAll) {
                if (!granted.contains(permissionCode)) {
                    throw new PermissionDeniedException(
                            "Missing required permission", permissionCode);
                }
            }

            if (!requireAny.isEmpty()) {
                boolean hasAtLeastOne = requireAny.stream().anyMatch(granted::contains);
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
