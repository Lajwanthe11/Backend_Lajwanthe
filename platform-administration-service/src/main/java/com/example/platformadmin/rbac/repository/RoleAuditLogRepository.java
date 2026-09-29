package com.example.platformadmin.rbac.repository;

import com.example.platformadmin.rbac.entity.RoleAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleAuditLogRepository
        extends JpaRepository<RoleAuditLog, UUID> {

    List<RoleAuditLog> findByUserIdOrderByPerformedAtDesc(
            UUID userId
    );
}