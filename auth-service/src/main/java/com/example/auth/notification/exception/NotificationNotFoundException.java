package com.example.auth.notification.exception;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Long notificationId) {
        super("Notification not found: " + notificationId);
    }
}