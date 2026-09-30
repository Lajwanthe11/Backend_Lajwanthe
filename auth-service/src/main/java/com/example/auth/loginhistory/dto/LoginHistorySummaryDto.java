package com.example.auth.loginhistory.dto;

/**
 * The Login Summary panel: today's attempts and the sessions active right now.
 */
public record LoginHistorySummaryDto(
        long todayLogins,
        long successfulLogins,
        long failedLogins,
        long activeSessions
) {
}
