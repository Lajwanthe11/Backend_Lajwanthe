package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class RoleAssignmentRequest {

    @NotNull
    private UUID roleId;

    private boolean primary;

    @NotNull
    private LocalDate effectiveDate;

    private LocalDate expiryDate;
}