package com.example.auth.notification.controller;

import com.example.auth.notification.dto.CreateNotificationRequest;
import com.example.auth.notification.dto.NotificationResponse;
import com.example.auth.notification.entity.Notification;
import com.example.auth.notification.service.NotificationDeliveryService;
import com.example.auth.notification.service.NotificationService;
import com.example.common.abstracts.AbstractController;
import com.example.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController extends AbstractController<
        Notification,
        Long,
        CreateNotificationRequest,
        NotificationResponse> {

    private final NotificationService notificationService;
    private final NotificationDeliveryService notificationDeliveryService;

    public NotificationController(
            NotificationService notificationService,
            NotificationDeliveryService notificationDeliveryService) {

        super(notificationService);

        this.notificationService = notificationService;
        this.notificationDeliveryService =
                notificationDeliveryService;
    }

    /**
     * Create notification and attempt delivery.
     *
     * We override the generic AbstractController create()
     * because notification creation has an additional
     * delivery workflow.
     */
    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> create(
            @Valid @RequestBody CreateNotificationRequest request) {

        NotificationResponse created =
                notificationService.create(request);

        Notification notification =
                notificationService.getNotification(
                        created.id()
                );

        notification =
                notificationDeliveryService.deliver(
                        notification
                );

        NotificationResponse response =
                notificationService.toResponseForController(
                        notification
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.created(
                                "Notification created successfully",
                                response
                        )
                );
    }

    /**
     * Get notifications for a specific user.
     *
     * ADMIN-only because the current JWT does not provide
     * a verified numeric user-id mapping.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>>
    getUserNotifications(
            @PathVariable Long userId) {

        List<NotificationResponse> notifications =
                notificationService
                        .getUserNotifications(userId)
                        .stream()
                        .map(
                                notificationService
                                        ::toResponseForController
                        )
                        .toList();

        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications for a specific user.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>>
    getUnreadNotifications(
            @PathVariable Long userId) {

        List<NotificationResponse> notifications =
                notificationService
                        .getUnreadNotifications(userId)
                        .stream()
                        .map(
                                notificationService
                                        ::toResponseForController
                        )
                        .toList();

        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark notification as READ.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationService.markAsRead(
                        notificationId
                );

        return ResponseEntity.ok(
                notificationService
                        .toResponseForController(notification)
        );
    }

    /**
     * Mark notification as SENT.
     */
    @PatchMapping("/{notificationId}/sent")
    public ResponseEntity<NotificationResponse> markAsSent(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationService.markAsSent(
                        notificationId
                );

        return ResponseEntity.ok(
                notificationService
                        .toResponseForController(notification)
        );
    }

    /**
     * Mark notification as FAILED.
     */
    @PatchMapping("/{notificationId}/failed")
    public ResponseEntity<NotificationResponse> markAsFailed(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationService.markAsFailed(
                        notificationId
                );

        return ResponseEntity.ok(
                notificationService
                        .toResponseForController(notification)
        );
    }

    /**
     * Retry a failed notification.
     */
    @PostMapping("/{notificationId}/retry")
    public ResponseEntity<NotificationResponse>
    retryNotification(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationDeliveryService.retry(
                        notificationId
                );

        return ResponseEntity.ok(
                notificationService
                        .toResponseForController(notification)
        );
    }
}
