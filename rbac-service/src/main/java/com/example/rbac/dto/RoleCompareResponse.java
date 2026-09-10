package com.example.rbac.dto;

import java.util.Set;

public record RoleCompareResponse(
        RoleSummary role1,
        RoleSummary role2,
        // All three sets are always present, possibly empty — never null.
        // Frontend renders "No permissions assigned" when a set is empty rather
        // than treating it as an error, since zero-permission roles are valid.
        Set<String> sharedPermissions,
        Set<String> onlyInRole1,
        Set<String> onlyInRole2
) {
    public record RoleSummary(Long id, String name, int totalPermissions) {}
}