package com.example.auth.notification.service;

import com.example.auth.notification.dto.CreateNotificationRequest;
import com.example.auth.notification.dto.NotificationResponse;
import com.example.auth.notification.dto.NotificationStatus;
import com.example.auth.notification.entity.Notification;
import com.example.auth.notification.exception.NotificationNotFoundException;
import com.example.auth.notification.repository.NotificationRepository;
import com.example.common.abstracts.AbstractService;
import com.example.common.tenant.TenantContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService extends AbstractService<
        Notification,
        Long,
        CreateNotificationRequest,
        NotificationResponse> {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {

        super(notificationRepository, "Notification");
        this.notificationRepository = notificationRepository;
    }

    /**
     * Convert request DTO to Notification entity.
     *
     * tenantId is deliberately NOT accepted from the request.
     * BaseEntity obtains the tenant from TenantContext.
     */
    @Override
    protected Notification toEntity(
            CreateNotificationRequest dto) {

        Notification notification = new Notification();

        notification.setUserId(dto.userId());
        notification.setUsername(dto.username());
        notification.setType(dto.type());
        notification.setChannel(dto.channel());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setTitle(dto.title());
        notification.setMessage(dto.message());
        notification.setAlertId(dto.alertId());

        return notification;
    }

    /**
     * Convert Notification entity to response DTO.
     */
    @Override
    protected NotificationResponse toDto(
            Notification notification) {

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

    /**
     * Public conversion method for notification-specific
     * controller operations.
     */
    public NotificationResponse toResponseForController(
            Notification notification) {

        return toDto(notification);
    }

    /**
     * Update notification fields.
     *
     * Status and read state are managed through their
     * dedicated operations.
     */
    @Override
    protected void updateEntityFromDto(
            Notification notification,
            CreateNotificationRequest dto) {

        notification.setUserId(dto.userId());
        notification.setUsername(dto.username());
        notification.setType(dto.type());
        notification.setChannel(dto.channel());
        notification.setTitle(dto.title());
        notification.setMessage(dto.message());
        notification.setAlertId(dto.alertId());
    }

    /**
     * Override the generic AbstractService getById().
     *
     * This is important because AbstractService normally uses
     * repository.findById(), while notifications must use the
     * tenant-aware lookup.
     */
    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(Long notificationId) {

        Notification notification =
                getNotification(notificationId);

        authorizeAccess(notification);

        return toDto(notification);
    }

    /**
     * Get all notifications for a user in the current tenant.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(
            Long userId) {

        String tenantId = TenantContext.getTenantId();

        return notificationRepository
                .findByTenantIdAndUserIdOrderByCreatedAtDesc(
                        tenantId,
                        userId
                );
    }

    /**
     * Get unread notifications for a user in the current tenant.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(
            Long userId) {

        String tenantId = TenantContext.getTenantId();

        return notificationRepository
                .findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(
                        tenantId,
                        userId
                );
    }

    /**
     * Tenant-aware notification lookup.
     *
     * A notification belonging to another tenant is treated
     * as not found.
     */
    @Transactional(readOnly = true)
    public Notification getNotification(
            Long notificationId) {

        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid notification ID"
            );
        }

        String tenantId = TenantContext.getTenantId();

        return notificationRepository
                .findByIdAndTenantId(
                        notificationId,
                        tenantId
                )
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                notificationId
                        ));
    }

    /**
     * Mark notification as READ.
     */
    @Transactional
    public Notification markAsRead(
            Long notificationId) {

        Notification notification =
                getNotification(notificationId);

        authorizeAccess(notification);

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Mark notification as SENT.
     */
    @Transactional
    public Notification markAsSent(
            Long notificationId) {

        Notification notification =
                getNotification(notificationId);

        authorizeAccess(notification);

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Mark notification as FAILED.
     */
    @Transactional
    public Notification markAsFailed(
            Long notificationId) {

        Notification notification =
                getNotification(notificationId);

        authorizeAccess(notification);

        notification.setStatus(NotificationStatus.FAILED);

        return notificationRepository.save(notification);
    }

    /**
     * Allows notification access only to:
     *
     * 1. The notification owner, or
     * 2. ROLE_ADMIN.
     *
     * Tenant isolation has already been enforced by
     * getNotification().
     */
    private void authorizeAccess(
            Notification notification) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(
                                "ROLE_ADMIN"::equals
                        );

        boolean isOwner =
                authentication.getName() != null
                        && authentication.getName()
                        .equals(notification.getUsername());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You do not have permission to access this notification"
            );
        }
    }
}