package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityEvent;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import com.example.auth.securityalerts.entity.SecurityEvent.SourceModule;

import java.time.LocalDateTime;

public record SecurityEventResponse(
        Long id,
        String tenantId,
        EventType eventType,
        SourceModule sourceModule,
        String userId,
        String username,
        Long companyId,
        String companyName,
        Long departmentId,
        String departmentName,
        String ipAddress,
        String deviceId,
        String deviceName,
        String userAgent,
        String location,
        String sessionId,
        String details,
        LocalDateTime occurredAt,
        LocalDateTime receivedAt,
        Long alertId,
        Long derivedFromEventId
) {

    public static SecurityEventResponse from(SecurityEvent e) {
        return new SecurityEventResponse(
                e.getId(), e.getTenantId(), e.getEventType(), e.getSourceModule(), e.getUserId(),
                e.getUsername(), e.getCompanyId(), e.getCompanyName(), e.getDepartmentId(),
                e.getDepartmentName(), e.getIpAddress(), e.getDeviceId(), e.getDeviceName(),
                e.getUserAgent(), e.getLocation(), e.getSessionId(), e.getDetails(), e.getOccurredAt(),
                e.getReceivedAt(), e.getAlertId(), e.getDerivedFromEventId());
    }
}
