package com.example.auth.securityalerts.dto;

import com.example.auth.securityalerts.entity.SecurityAlert.AlertType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Numbers for the "Alert Summary" panel and the dashboard cards, for the same filters as the alert list.
 * lastUpdatedAt / lastUpdatedBy are the "Audit Information": the most recently changed alert in scope.
 */
public record SecurityAlertSummaryResponse(
        long totalAlerts,
        long openAlerts,
        long acknowledgedAlerts,
        long resolvedAlerts,
        long closedAlerts,
        long criticalAlerts,
        long highSeverityAlerts,
        long mediumSeverityAlerts,
        long lowSeverityAlerts,
        long activeCriticalAlerts,
        long resolvedToday,
        Map<AlertType, Long> alertsByType,
        LocalDateTime lastUpdatedAt,
        String lastUpdatedBy
) {
}
