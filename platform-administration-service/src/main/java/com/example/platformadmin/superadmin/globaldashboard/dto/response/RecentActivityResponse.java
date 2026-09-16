package com.example.platformadmin.superadmin.globaldashboard.dto.response;

import java.time.LocalDateTime;

public record RecentActivityResponse(
        String activityId,
        String description,
        String performedBy,
        String moduleName,
        LocalDateTime activityDateTime
) {
}