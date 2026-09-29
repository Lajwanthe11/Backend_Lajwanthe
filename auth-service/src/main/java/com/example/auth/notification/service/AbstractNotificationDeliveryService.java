package com.example.auth.notification.service;

import com.example.auth.notification.entity.Notification;

public abstract class AbstractNotificationDeliveryService {

    /**
     * Defines the delivery operation that concrete notification
     * delivery services must implement.
     */
    public abstract Notification deliver(Notification notification);

    /**
     * Common retry workflow for notification delivery.
     */
    public abstract Notification retry(Long notificationId);
}