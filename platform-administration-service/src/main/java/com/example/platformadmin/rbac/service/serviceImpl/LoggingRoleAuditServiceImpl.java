package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.service.RoleAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LoggingRoleAuditServiceImpl implements RoleAuditService {

    private static final Logger log = LoggerFactory.getLogger(LoggingRoleAuditServiceImpl.class);

    @Override
    public void record(
            UUID tenantId,
            UUID actorId,
            UUID userId,
            UUID roleId,
            String action,
            String reason
    ) {
        // Replace this logging implementation with the shared immutable role_audit_log
        // service when that Sprint 2 module is merged into the common project.
        log.info(
                "RBAC_AUDIT tenantId={} actorId={} userId={} roleId={} action={} reason={}",
                tenantId, actorId, userId, roleId, action, reason
        );
    }
}
