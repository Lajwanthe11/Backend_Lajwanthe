package com.example.platformadmin.rbac.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserRoleResponse {

    private UUID userRoleId;

    private UUID userId;

    private UUID roleId;

    private UUID tenantId;

    private boolean primary;

    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private UUID assignedBy;

    private LocalDateTime assignedAt;

    private boolean active;
}