package com.example.platformadmin.superadmin.platform_settings_service.integration.impl;

import com.example.platformadmin.superadmin.platform_settings_service.integration.PlatformSettingsAuditIntegration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Local adapter used until the shared Audit service contract is supplied.
 * The authoritative persisted history is written by the Team 4 service transaction;
 * this adapter provides the shared-audit integration seam and operational trace.
 */
@Slf4j
@Component
public class HistoryBackedPlatformSettingsAuditIntegration implements PlatformSettingsAuditIntegration {

    @Override
    public void record(AuditEvent event) {

        log.info("Platform settings audit event recorded. module={}, entityType={}, entityId={}, " +
                        "action={}, actorId={}, status={}, version={}",

                event.module(),
                event.entityType(),
                event.entityId(),
                event.action(),
                event.actorId(),
                event.status(),
                event.configurationVersion());
    }

}