package com.example.auth.notification.service;

import com.example.auth.notification.NotificationChannel;
import com.example.auth.notification.exception.NotificationNotFoundException;
import com.example.auth.notification.NotificationStatus;
import com.example.auth.notification.NotificationType;
import com.example.auth.notification.entity.Notification;
import com.example.auth.notification.repository.NotificationRepository;
import com.example.common.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates a notification for the current tenant.
     *
     * Tenant ID is obtained from TenantContext and is not accepted
     * from the client request.
     */
    @Transactional
    public Notification createNotification(
            Long userId,
            String username,
            NotificationType type,
            NotificationChannel channel,
            String title,
            String message,
            Long alertId) {

        String tenantId = TenantContext.getTenantId();

        Notification notification = new Notification();

        notification.setUserId(userId);
        notification.setTenantId(tenantId);
        notification.setUsername(username);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setAlertId(alertId);

        return notificationRepository.save(notification);
    }

    /**
     * Gets all notifications for a user within the current tenant.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {

        String tenantId = TenantContext.getTenantId();

        return notificationRepository
                .findByTenantIdAndUserIdOrderByCreatedAtDesc(
                        tenantId,
                        userId
                );
    }

    /**
     * Gets unread notifications for a user within the current tenant.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {

        String tenantId = TenantContext.getTenantId();

        return notificationRepository
                .findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(
                        tenantId,
                        userId
                );
    }

    /**
     * Gets a notification only if it belongs to the current tenant.
     */
    @Transactional(readOnly = true)
    public Notification getNotification(Long notificationId) {

        String tenantId = TenantContext.getTenantId();

        return notificationRepository
                .findByIdAndTenantId(notificationId, tenantId)
                .orElseThrow(() ->
                        new NotificationNotFoundException(notificationId));
    }

    /**
     * Marks a notification as read.
     */
    @Transactional
    public Notification markAsRead(Long notificationId) {

        Notification notification = getNotification(notificationId);

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Marks a notification as successfully sent.
     */
    @Transactional
    public Notification markAsSent(Long notificationId) {

        Notification notification = getNotification(notificationId);

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Marks a notification as failed.
     */
    @Transactional
    public Notification markAsFailed(Long notificationId) {

        Notification notification = getNotification(notificationId);

        notification.setStatus(NotificationStatus.FAILED);

        return notificationRepository.save(notification);
    }
}