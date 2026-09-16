package com.example.platformadmin.superadmin.superadmindashboard.dto.response;

import lombok.Data;

@Data
public class SecurityOverviewResponse {
    private Long failedLoginAttempts;
    private Long lockedAccounts;
    private Long securityAlerts;
    private Long activeSessions;
    private Long auditEvents;
}