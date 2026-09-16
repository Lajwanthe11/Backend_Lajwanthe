package com.example.platformadmin.superadmin.superadmindashboard.integration;

import java.time.Instant;
import java.util.List;

/**
 * Client for calling the Audit/Activity Log service (owned by another
 * team). Real implementation should use RestClient + Eureka once their
 * API is ready. For now, see {@link AuditLogClientStub}.
 */
public interface AuditLogClient {

    List<SecurityAlertRecord> getRecentSecurityAlerts(int limit);

    List<ActivityRecord> getRecentActivities(int limit);

    record SecurityAlertRecord(
            String alertId,
            String severity, // e.g. "LOW", "MEDIUM", "HIGH", "CRITICAL"
            String message,
            Instant occurredAt
    ) {
    }

    record ActivityRecord(
            String activityId,
            String description, // e.g. "Platform configuration updated"
            String performedBy,
            Instant occurredAt
    ) {
    }
}