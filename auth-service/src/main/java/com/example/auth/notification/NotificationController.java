package com.example.auth.notification;

import com.example.auth.notification.dto.CreateNotificationRequest;
import com.example.auth.notification.dto.NotificationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationDeliveryService notificationDeliveryService;

    public NotificationController(
            NotificationService notificationService,
            NotificationDeliveryService notificationDeliveryService) {

        this.notificationService = notificationService;
        this.notificationDeliveryService = notificationDeliveryService;
    }

    /**
     * Create a new notification and attempt delivery.
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        Notification notification =
                notificationService.createNotification(
                        request.userId(),
                        request.username(),
                        request.type(),
                        request.channel(),
                        request.title(),
                        request.message(),
                        request.alertId()
                );

        /*
         * Attempt delivery after the notification has been stored.
         */
        notification =
                notificationDeliveryService.deliver(notification);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(notification));
    }

    /**
     * Get all notifications for a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId) {

        List<NotificationResponse> notifications =
                notificationService
                        .getUserNotifications(userId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for a user.
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @PathVariable Long userId) {

        List<NotificationResponse> notifications =
                notificationService
                        .getUnreadNotifications(userId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark notification as read.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }

    /**
     * Mark notification as successfully sent.
     */
    @PatchMapping("/{notificationId}/sent")
    public ResponseEntity<NotificationResponse> markAsSent(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationService.markAsSent(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }

    /**
     * Mark notification as failed.
     */
    @PatchMapping("/{notificationId}/failed")
    public ResponseEntity<NotificationResponse> markAsFailed(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationService.markAsFailed(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }
    @PostMapping("/{notificationId}/retry")
    public ResponseEntity<NotificationResponse> retryNotification(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationDeliveryService.retry(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationService.getNotification(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }
    /**
     * Converts entity to API response.
     */
    /**
     * Converts entity to API response.
     */
    private NotificationResponse toResponse(Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getTenantId(),
                notification.getUsername(),
                notification.getType(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getAlertId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getReadAt()
        );
    }
}