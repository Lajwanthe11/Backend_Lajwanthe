package com.example.auth.notification.repository;

import com.example.auth.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByTenantIdAndUserIdOrderByCreatedAtDesc(
            String tenantId,
            Long userId
    );

    List<Notification> findByTenantIdAndUserIdAndReadFalseOrderByCreatedAtDesc(
            String tenantId,
            Long userId
    );

    Optional<Notification> findByIdAndTenantId(
            Long notificationId,
            String tenantId
    );
}