package com.enterprise.superadmin.platform_branding_service.integration.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("dev")
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
                "LOCAL AUDIT | actor={} action={} module={} entity={} entityId={} status={} timestamp={}",
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