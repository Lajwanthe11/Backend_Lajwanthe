package com.example.auth.audit.repository;

import java.util.List;

import com.example.auth.audit.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository
        extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}