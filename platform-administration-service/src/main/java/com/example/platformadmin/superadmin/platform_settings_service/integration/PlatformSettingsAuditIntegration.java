package com.example.platformadmin.superadmin.platform_settings_service.integration;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Internal contract for the shared audit capability.
 * The Team document defines the audit contract but does not define an HTTP
 * endpoint or client schema. Keep that external detail behind this interface.
 */
public interface PlatformSettingsAuditIntegration {

    void record(AuditEvent event);

    record AuditEvent(
            String actorId,
            String actorName,
            String action,
            String module,
            String entityType,
            UUID entityId,
            String status,
            Long configurationVersion,
            OffsetDateTime timestamp,
            String ipAddress) {
    }

}