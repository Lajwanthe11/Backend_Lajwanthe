package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityAlert;
import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;
import com.example.auth.securityalerts.entity.SecurityAlert.ResolutionStatus;
import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import com.example.auth.securityalerts.entity.SecurityAlert.Status;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import com.example.auth.securityalerts.entity.SecurityEvent.SourceModule;

import java.time.LocalDateTime;

/**
 * Full alert as administrators see it. id is the numeric key used in URLs;
 * alertCode (SA-000042) is the "Alert ID" shown on screen.
 */
public record SecurityAlertResponse(
        Long id,
        String alertCode,
        String tenantId,

        // Alert information
        AlertType alertType,
        EventType eventType,
        Severity severity,
        Status status,
        String title,
        String description,
        SourceModule sourceModule,
        Long policyId,
        LocalDateTime alertTime,
        LocalDateTime lastOccurredAt,
        int occurrenceCount,

        // Affected user information
        String userId,
        String username,
        Long companyId,
        String companyName,
        Long departmentId,
        String departmentName,
        String deviceId,
        String deviceName,
        String ipAddress,
        String location,

        // Alert resolution
        ResolutionStatus resolutionStatus,
        String resolutionRemarks,
        LocalDateTime resolutionTime,
        String resolvedBy,
        String acknowledgedBy,
        LocalDateTime acknowledgedAt,
        String closedBy,
        LocalDateTime closedAt,

        // Audit information
        LocalDateTime createdAt,
        LocalDateTime lastUpdatedAt,
        String lastUpdatedBy
) {

    public static SecurityAlertResponse from(SecurityAlert a) {
        return new SecurityAlertResponse(
                a.getId(), a.getAlertCode(), a.getTenantId(),
                a.getAlertType(), a.getEventType(), a.getSeverity(), a.getStatus(), a.getTitle(),
                a.getDescription(), a.getSourceModule(), a.getPolicyId(), a.getAlertTime(),
                a.getLastOccurredAt(), a.getOccurrenceCount(),
                a.getUserId(), a.getUsername(), a.getCompanyId(), a.getCompanyName(), a.getDepartmentId(),
                a.getDepartmentName(), a.getDeviceId(), a.getDeviceName(), a.getIpAddress(), a.getLocation(),
                a.getResolutionStatus(), a.getResolutionRemarks(), a.getResolutionTime(), a.getResolvedBy(),
                a.getAcknowledgedBy(), a.getAcknowledgedAt(), a.getClosedBy(), a.getClosedAt(),
                a.getCreatedAt(), a.getUpdatedAt(), a.getUpdatedBy());
    }
}
