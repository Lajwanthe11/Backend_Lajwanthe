package com.example.auth.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeliveryService {

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