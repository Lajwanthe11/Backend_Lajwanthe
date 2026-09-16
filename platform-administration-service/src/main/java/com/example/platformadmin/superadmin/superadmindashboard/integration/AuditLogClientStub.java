package com.example.platformadmin.superadmin.superadmindashboard.integration;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * TEMPORARY stub - returns mock data so the dashboard pipeline can be built
 * and tested before the Audit Log service is available.
 *
 * TODO: replace with a real RestClient-based implementation once confirmed.
 */
@Component
public class AuditLogClientStub implements AuditLogClient {

    @Override
    public List<SecurityAlertRecord> getRecentSecurityAlerts(int limit) {
        List<SecurityAlertRecord> mockAlerts = List.of(
                new SecurityAlertRecord("alert-501", "HIGH",
                        "Multiple failed login attempts detected",
                        Instant.now().minus(30, ChronoUnit.MINUTES)),
                new SecurityAlertRecord("alert-502", "MEDIUM",
                        "New device login from unrecognized location",
                        Instant.now().minus(2, ChronoUnit.HOURS))
        );
        return mockAlerts.stream().limit(limit).toList();
    }

    @Override
    public List<ActivityRecord> getRecentActivities(int limit) {
        List<ActivityRecord> mockActivities = List.of(
                new ActivityRecord("act-901", "Platform configuration updated",
                        "super.admin", Instant.now().minus(1, ChronoUnit.HOURS)),
                new ActivityRecord("act-902", "New organization onboarded",
                        "super.admin", Instant.now().minus(3, ChronoUnit.HOURS)),
                new ActivityRecord("act-903", "License renewed",
                        "system", Instant.now().minus(5, ChronoUnit.HOURS)),
                new ActivityRecord("act-904", "Feature enabled successfully",
                        "super.admin", Instant.now().minus(6, ChronoUnit.HOURS))
        );
        return mockActivities.stream().limit(limit).toList();
    }
}