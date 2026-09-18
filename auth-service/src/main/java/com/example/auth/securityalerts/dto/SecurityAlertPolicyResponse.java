package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;
import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import com.example.auth.securityalerts.entity.SecurityAlertPolicy;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;

import java.time.LocalDateTime;

/** scope is GLOBAL for a platform-wide default, TENANT for a tenant's own policy. */
public record SecurityAlertPolicyResponse(
        Long id,
        String scope,
        String tenantId,
        String name,
        String description,
        EventType eventType,
        AlertType alertType,
        Severity severity,
        int threshold,
        int timeWindowMinutes,
        int suppressionMinutes,
        boolean enabled,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime updatedAt,
        String updatedBy
) {

    public static SecurityAlertPolicyResponse from(SecurityAlertPolicy p) {
        return new SecurityAlertPolicyResponse(
                p.getId(), p.isGlobal() ? "GLOBAL" : "TENANT", p.getTenantId(), p.getName(), p.getDescription(),
                p.getEventType(), p.getAlertType(), p.getSeverity(), p.getThreshold(), p.getTimeWindowMinutes(),
                p.getSuppressionMinutes(), p.isEnabled(), p.getCreatedAt(), p.getCreatedBy(),
                p.getUpdatedAt(), p.getUpdatedBy());
    }
}
