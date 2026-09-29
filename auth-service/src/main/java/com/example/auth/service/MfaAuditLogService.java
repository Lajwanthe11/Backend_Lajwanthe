package com.example.auth.service;

import com.example.auth.entity.MfaAuditLog;
import com.example.auth.repository.MfaAuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MfaAuditLogService {

    private final MfaAuditLogRepository auditLogRepository;

    public MfaAuditLogService(
            MfaAuditLogRepository auditLogRepository) {

        this.auditLogRepository =
                auditLogRepository;
    }

    // ---------------------------------------------------------------
    // Create Audit Log
    // ---------------------------------------------------------------

    public MfaAuditLog logEvent(
            String username,
            String organizationId,
            String eventType,
            boolean success,
            String details,
            String ipAddress) {

        MfaAuditLog auditLog =
                new MfaAuditLog();

        auditLog.setUsername(username);
        auditLog.setOrganizationId(organizationId);
        auditLog.setEventType(eventType);
        auditLog.setSuccess(success);
        auditLog.setDetails(details);
        auditLog.setIpAddress(ipAddress);
        auditLog.setCreatedAt(
                LocalDateTime.now()
        );

        return auditLogRepository.save(
                auditLog
        );
    }

    // ---------------------------------------------------------------
    // Get User Audit Logs
    // ---------------------------------------------------------------

    public List<MfaAuditLog> getUserAuditLogs(
            String username) {

        return auditLogRepository
                .findByUsernameOrderByCreatedAtDesc(
                        username
                );
    }

    // ---------------------------------------------------------------
    // Get Organization Audit Logs
    // ---------------------------------------------------------------

    public List<MfaAuditLog> getOrganizationAuditLogs(
            String organizationId) {

        return auditLogRepository
                .findByOrganizationIdOrderByCreatedAtDesc(
                        organizationId
                );
    }
}

