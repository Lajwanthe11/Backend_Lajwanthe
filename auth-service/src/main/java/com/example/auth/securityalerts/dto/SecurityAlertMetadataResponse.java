package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;
import com.example.auth.securityalerts.entity.SecurityAlert.ResolutionStatus;
import com.example.auth.securityalerts.entity.SecurityAlert.Severity;
import com.example.auth.securityalerts.entity.SecurityAlert.Status;
import com.example.auth.securityalerts.entity.SecurityEvent.EventType;
import com.example.auth.securityalerts.entity.SecurityEvent.SourceModule;

import java.util.Arrays;
import java.util.List;

/** Values for the screen's dropdowns, so the frontend does not hard-code them. */
public record SecurityAlertMetadataResponse(
        List<AlertType> alertTypes,
        List<Severity> severities,
        List<Status> statuses,
        List<ResolutionStatus> resolutionStatuses,
        List<EventTypeInfo> eventTypes,
        List<SourceModule> sourceModules
) {

    public record EventTypeInfo(EventType name, AlertType alertType, Severity defaultSeverity, String title) {
    }

    public static SecurityAlertMetadataResponse create() {
        return new SecurityAlertMetadataResponse(
                List.of(AlertType.values()),
                List.of(Severity.values()),
                List.of(Status.values()),
                List.of(ResolutionStatus.values()),
                Arrays.stream(EventType.values())
                        .map(t -> new EventTypeInfo(t, t.getAlertType(), t.getDefaultSeverity(), t.getTitle()))
                        .toList(),
                List.of(SourceModule.values()));
    }
}
