package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityAlert;
import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;
import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import com.example.auth.securityalerts.entity.SecurityAlert.Status;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;

import java.time.LocalDateTime;

/**
 * An alert about the caller's own account. Leaves out the investigation details
 * (remarks, who handled it) that only administrators may see.
 */
public record UserSecurityAlertResponse(
        Long id,
        String alertCode,
        AlertType alertType,
        EventType eventType,
        Severity severity,
        Status status,
        String title,
        LocalDateTime alertTime,
        LocalDateTime lastOccurredAt,
        int occurrenceCount,
        String deviceName,
        String ipAddress,
        String location,
        LocalDateTime resolutionTime
) {

    public static UserSecurityAlertResponse from(SecurityAlert a) {
        return new UserSecurityAlertResponse(
                a.getId(), a.getAlertCode(), a.getAlertType(), a.getEventType(), a.getSeverity(),
                a.getStatus(), a.getTitle(), a.getAlertTime(), a.getLastOccurredAt(), a.getOccurrenceCount(),
                a.getDeviceName(), a.getIpAddress(), a.getLocation(), a.getResolutionTime());
    }
}
