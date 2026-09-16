package com.example.rbac.dto;

import java.util.UUID;

import java.util.Set;

public record RoleCompareResponse(
        RoleSummary role1,
        RoleSummary role2,
        Set<String> sharedPermissions,
        Set<String> onlyInRole1,
        Set<String> onlyInRole2
) {
    public record RoleSummary(UUID id, String name, int totalPermissions) {}
}