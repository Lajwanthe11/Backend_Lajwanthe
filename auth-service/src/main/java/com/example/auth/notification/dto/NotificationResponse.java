package com.example.auth.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        String tenantId,
        String username,
        NotificationType type,
        NotificationChannel channel,
        NotificationStatus status,
        String title,
        String message,
        Long alertId,
        boolean read,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        LocalDateTime readAt
) {
}