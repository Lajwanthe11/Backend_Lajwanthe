package com.example.rbac.dto;

import com.example.rbac.enums.RoleType;

import java.time.LocalDate;
import java.util.UUID;

public record ExpiringRoleAssignment(
        UUID userRoleId,
        UUID userId,
        UUID roleId,
        String roleCode,
        String roleName,
        RoleType roleType,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        long daysToExpiry
) {
}
