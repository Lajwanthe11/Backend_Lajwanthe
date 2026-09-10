package com.example.rbac.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StubPermissionResolver implements PermissionResolver {

    private final Map<String, Set<String>> permissionsByUserId = new ConcurrentHashMap<>();

    public StubPermissionResolver() {
        // "HR manager" style user
        permissionsByUserId.put("user-hr-1", Set.of(
                "USER_READ", "USER_CREATE", "USER_UPDATE",
                "REPORT_VIEW"));
        // read-only user
        permissionsByUserId.put("user-readonly-1", Set.of(
                "USER_READ", "REPORT_VIEW"));
        // admin
        permissionsByUserId.put("user-admin-1", Set.of(
                "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
                "REPORT_VIEW", "REPORT_EXPORT", "SECURITY_EVENTS_VIEW"));
        // trusted internal service account, used to call
        // POST /api/v1/rbac/access/validate on behalf of other services
        permissionsByUserId.put("svc-internal-1", Set.of("INTERNAL_SERVICE"));
    }

    @Override
    public Set<String> resolvePermissions(String userId, String tenantId) {
        return permissionsByUserId.getOrDefault(userId, Set.of());
    }
}
