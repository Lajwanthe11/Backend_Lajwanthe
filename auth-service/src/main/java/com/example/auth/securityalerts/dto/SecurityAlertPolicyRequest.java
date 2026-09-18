package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;
import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * "Create Alert Policy" and policy updates. Fields left null take the event type's default.
 *
 * global = true creates a platform-wide default (Super Administrator only). Otherwise the policy
 * belongs to the caller's tenant; a Super Administrator may name another tenant in tenantId.
 */
public record SecurityAlertPolicyRequest(
        @NotBlank(message = "Policy name is required") @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotNull(message = "eventType is required") EventType eventType,
        AlertType alertType,
        @NotNull(message = "severity is required") Severity severity,
        @Min(1) @Max(1000) Integer threshold,
        @Min(1) @Max(10080) Integer timeWindowMinutes,
        @Min(0) @Max(10080) Integer suppressionMinutes,
        Boolean enabled,
        Boolean global,
        @Size(max = 100) String tenantId
) {
}
