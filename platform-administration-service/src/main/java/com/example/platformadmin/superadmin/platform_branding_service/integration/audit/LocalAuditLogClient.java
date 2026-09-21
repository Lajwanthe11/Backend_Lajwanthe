package com.example.platformadmin.superadmin.platform_branding_service.integration.audit;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalAuditLogClient implements AuditLogClient {

    private static final Logger log =
            LoggerFactory.getLogger(LocalAuditLogClient.class);

    @Override
    public void record(
            String actorId,
            String action,
            String module,
            String entity,
            String entityId,
            String status,
            LocalDateTime auditTimestamp) {

        log.info(
                "AUDIT | actorId={} | action={} | module={} | entity={} | entityId={} | status={} | timestamp={}",
                actorId,
                action,
                module,
                entity,
                entityId,
                status,
                auditTimestamp
        );
    }
}