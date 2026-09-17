package com.example.platformadmin.superadmin.globaldashboard.integration.dto;

public record UserSummaryResponse(
        long totalUsers,
        long activeUsers,
        long onlineUsers
) {
}