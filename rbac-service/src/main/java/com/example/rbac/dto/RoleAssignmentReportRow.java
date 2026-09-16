package com.example.rbac.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

public record RoleAssignmentReportRow(
        UUID userRoleId,
        UUID userId,
        UUID roleId,
        String roleCode,
        String roleName,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        String status,
        boolean primaryRole,
        LocalDateTime assignedAt
) {
}
