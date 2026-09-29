package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record RuleTestRequest(
        UUID userId,
        UUID roleId,
        @NotBlank String resourceType,
        Map<String, Object> sampleData,
        Map<String, Object> userContext
) {
}

