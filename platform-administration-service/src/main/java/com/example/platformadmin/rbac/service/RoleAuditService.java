package com.example.platformadmin.rbac.service;

import java.util.UUID;

/**
 * Records role-assignment audit events for Part 4.
 * The default implementation logs the event until the shared audit module is merged.
 */
public interface RoleAuditService {

    void record(
            UUID tenantId,
            UUID actorId,
            UUID userId,
            UUID roleId,
            String action,
            String reason
    );
}
