package com.example.auth.notification.dto;

import com.example.auth.notification.NotificationChannel;
import com.example.auth.notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(

        @NotNull
        Long userId,

        @NotBlank
        @Size(max = 100)
        String username,

        @NotNull
        NotificationType type,

        @NotNull
        NotificationChannel channel,

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 2000)
        String message,

        Long alertId
) {
}