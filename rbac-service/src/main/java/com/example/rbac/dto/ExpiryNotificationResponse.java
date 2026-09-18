package com.example.rbac.dto;

import java.time.LocalDate;

public record ExpiryNotificationResponse(
        LocalDate targetExpiryDate,
        int matchedAssignments,
        int notificationsTriggered
) {
}
