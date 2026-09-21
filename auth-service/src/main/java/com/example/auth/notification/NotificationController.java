package com.example.auth.notification;

import com.example.auth.notification.dto.CreateNotificationRequest;
import com.example.auth.notification.dto.NotificationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
     *
     * ADMIN-only: there is currently no verified link between the
     * authenticated principal and this numeric userId (auth-service's
     * JWT carries username/roles/tenantId only, not a user id), so this
     * cannot yet be safely opened up as a self-service "my notifications"
     * endpoint. Restricting to ROLE_ADMIN until that identity link exists.
     */
    @PreAuthorize("hasRole('ADMIN')")
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
     *
     * ADMIN-only — see the note on getUserNotifications() above.
     */
    @PreAuthorize("hasRole('ADMIN')")
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
     *
     * Ownership check: allowed for the notification's own creator
     * (authenticated username == notification.username) or ROLE_ADMIN.
     * Without this, any authenticated user in the same tenant could
     * mutate another user's notification just by guessing its id.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId) {

        authorizeAccess(notificationService.getNotification(notificationId));

        Notification notification =
                notificationService.markAsRead(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }

    /**
     * Mark notification as successfully sent.
     *
     * Ownership check — see the note on markAsRead() above.
     */
    @PatchMapping("/{notificationId}/sent")
    public ResponseEntity<NotificationResponse> markAsSent(
            @PathVariable Long notificationId) {

        authorizeAccess(notificationService.getNotification(notificationId));

        Notification notification =
                notificationService.markAsSent(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }

    /**
     * Mark notification as failed.
     *
     * Ownership check — see the note on markAsRead() above.
     */
    @PatchMapping("/{notificationId}/failed")
    public ResponseEntity<NotificationResponse> markAsFailed(
            @PathVariable Long notificationId) {

        authorizeAccess(notificationService.getNotification(notificationId));

        Notification notification =
                notificationService.markAsFailed(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }

    /**
     * Retry a failed notification.
     *
     * Ownership check — see the note on markAsRead() above.
     */
    @PostMapping("/{notificationId}/retry")
    public ResponseEntity<NotificationResponse> retryNotification(
            @PathVariable Long notificationId) {

        authorizeAccess(notificationService.getNotification(notificationId));

        Notification notification =
                notificationDeliveryService.retry(notificationId);

        return ResponseEntity.ok(toResponse(notification));
    }

    /**
     * Get a single notification.
     *
     * Ownership check — see the note on markAsRead() above.
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotification(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationService.getNotification(notificationId);

        authorizeAccess(notification);

        return ResponseEntity.ok(toResponse(notification));
    }

    /**
     * Allows access only to the notification's own creator (by username)
     * or a caller with ROLE_ADMIN. Tenant scoping already happened inside
     * notificationService.getNotification() — this is a second, narrower
     * check on top of that, so a same-tenant user can no longer act on a
     * different user's notification just by knowing/guessing its id.
     */
    private void authorizeAccess(Notification notification) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        boolean isOwner = authentication != null
                && authentication.getName() != null
                && authentication.getName().equals(notification.getUsername());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You do not have permission to access this notification");
        }
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