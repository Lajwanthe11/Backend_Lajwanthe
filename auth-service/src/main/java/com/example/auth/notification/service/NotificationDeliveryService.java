package com.example.auth.notification.service;

import com.example.auth.notification.NotificationStatus;
import com.example.auth.notification.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final NotificationService notificationService;

    public NotificationDeliveryService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Transactional
    public Notification deliver(Notification notification) {

        try {

            switch (notification.getChannel()) {

                case IN_APP:
                    return deliverInApp(notification);

                case EMAIL:
                    return prepareEmailDelivery(notification);

                default:
                    return notificationService.markAsFailed(
                            notification.getId()
                    );
            }

        } catch (Exception exception) {

            // Log the real cause before masking it as a delivery failure —
            // previously this was swallowed silently, so a genuine bug
            // (bad data, a downstream outage, etc.) was indistinguishable
            // from an expected delivery failure.
            log.error(
                    "Notification delivery threw an unexpected exception for notification id={}, channel={}",
                    notification.getId(),
                    notification.getChannel(),
                    exception
            );

            return notificationService.markAsFailed(
                    notification.getId()
            );
        }
    }

    @Transactional
    public Notification retry(Long notificationId) {

        Notification notification =
                notificationService.getNotification(notificationId);

        if (notification.getStatus() != NotificationStatus.FAILED) {
            return notification;
        }

        notification.setStatus(NotificationStatus.PENDING);
        notification.setSentAt(null);

        return deliver(notification);
    }

    private Notification deliverInApp(Notification notification) {

        return notificationService.markAsSent(
                notification.getId()
        );
    }

    private Notification prepareEmailDelivery(Notification notification) {

        return notification;
    }
}