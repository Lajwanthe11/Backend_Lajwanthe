package com.example.rbac.dto;

import java.time.Instant;

public record RoleHistoryDto(
        String changedByName,
        Instant changedAt,
        String changeType,
        String fieldName,
        String oldValue,
        String newValue
) {}