package com.example.platformadmin.rbac.dto.response;

import java.util.UUID;
import java.util.Set;

public class RoleCompareResponse {

    private RoleSummary role1;
    private RoleSummary role2;
    private Set<String> sharedPermissions;
    private Set<String> onlyInRole1;
    private Set<String> onlyInRole2;

    public RoleCompareResponse(RoleSummary role1, RoleSummary role2,
                               Set<String> sharedPermissions,
                               Set<String> onlyInRole1,
                               Set<String> onlyInRole2) {
        this.role1 = role1;
        this.role2 = role2;
        this.sharedPermissions = sharedPermissions;
        this.onlyInRole1 = onlyInRole1;
        this.onlyInRole2 = onlyInRole2;
    }

    public RoleSummary getRole1() { return role1; }
    public RoleSummary getRole2() { return role2; }
    public Set<String> getSharedPermissions() { return sharedPermissions; }
    public Set<String> getOnlyInRole1() { return onlyInRole1; }
    public Set<String> getOnlyInRole2() { return onlyInRole2; }

    public static class RoleSummary {
        private UUID id;
        private String roleName;
        private int permissionCount;

        public RoleSummary(UUID id, String roleName, int permissionCount) {
            this.id = id;
            this.roleName = roleName;
            this.permissionCount = permissionCount;
        }

        public UUID getId() { return id; }
        public String getRoleName() { return roleName; }
        public int getPermissionCount() { return permissionCount; }
    }
}