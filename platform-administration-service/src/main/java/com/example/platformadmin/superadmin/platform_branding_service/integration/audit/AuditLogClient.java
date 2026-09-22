package com.example.platformadmin.superadmin.platform_branding_service.integration.audit;

import java.time.LocalDateTime;

/**
 * Integration contract for recording administrative audit events.
 *
 * <p>The Platform Branding module uses this contract to communicate
 * with the approved Audit capability. It does not directly access
 * the audit_logs table or repository.</p>
 */
public interface AuditLogClient {

    /**
     * Records an administrative audit event.
     *
     * @param actorId       authenticated user who performed the action
     * @param action        action performed
     * @param module        module where the action occurred
     * @param entity        affected business entity
     * @param entityId      identifier of the affected entity
     * @param status        execution status
     * @param auditTimestamp timestamp of the audit event
     */
    void record(
            String actorId,
            String action,
            String module,
            String entity,
            String entityId,
            String status,
            LocalDateTime auditTimestamp
    );
}