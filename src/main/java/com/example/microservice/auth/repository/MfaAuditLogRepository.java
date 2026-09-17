package com.example.microservice.auth.repository;

import com.example.microservice.auth.entity.MfaAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MfaAuditLogRepository
        extends JpaRepository<MfaAuditLog, Long> {

    List<MfaAuditLog> findByUsernameOrderByCreatedAtDesc(
            String username
    );

    List<MfaAuditLog> findByOrganizationIdOrderByCreatedAtDesc(
            String organizationId
    );
}


