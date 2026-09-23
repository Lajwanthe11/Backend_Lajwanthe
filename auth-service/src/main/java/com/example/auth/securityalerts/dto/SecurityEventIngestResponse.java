package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;

import java.util.List;

/**
 * Result of recording one event.
 * alerts lists every alert the event raised or joined, including alerts from events that
 * threat detection derived from it (for example MULTIPLE_IP_SESSIONS from a SESSION_CREATED).
 */
public record SecurityEventIngestResponse(
        Long eventId,
        boolean monitoringEnabled,
        List<RaisedAlert> alerts
) {

    public record RaisedAlert(
            Long eventId,
            EventType eventType,
            Long alertId,
            String alertCode,
            Severity severity,
            boolean newAlert
    ) {
    }

    public static SecurityEventIngestResponse disabled() {
        return new SecurityEventIngestResponse(null, false, List.of());
    }
}
