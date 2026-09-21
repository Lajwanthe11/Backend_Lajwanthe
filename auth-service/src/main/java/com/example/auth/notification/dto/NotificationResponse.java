package com.example.auth.notification.dto;

import com.example.auth.notification.NotificationChannel;
import com.example.auth.notification.NotificationStatus;
import com.example.auth.notification.NotificationType;

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