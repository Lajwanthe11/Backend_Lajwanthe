package com.platformadmin.superadmin.globaldashboard.dto.response;

import java.time.LocalDateTime;

public record GlobalDashboardNotificationResponse(
        String notificationId,
        String title,
        String message,
        String severity,
        LocalDateTime createdAt
) {
}