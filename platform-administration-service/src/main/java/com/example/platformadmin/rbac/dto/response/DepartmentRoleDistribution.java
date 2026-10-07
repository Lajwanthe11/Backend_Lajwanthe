package com.example.platformadmin.rbac.dto.response;

import java.util.UUID;

public record DepartmentRoleDistribution(
        UUID departmentId,
        UUID roleId,
        String roleCode,
        String roleName,
        long userCount
) {
}
