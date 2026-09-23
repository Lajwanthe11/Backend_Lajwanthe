package com.example.rbac.dto.response;

import java.time.LocalDate;

public record ExpiryNotificationResponse(
        LocalDate targetExpiryDate,
        int matchedAssignments,
        int notificationsTriggered
) {
}
