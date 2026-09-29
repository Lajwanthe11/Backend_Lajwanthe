package com.example.platformadmin.rbac.service;

import java.util.Optional;
import java.util.UUID;

/**
 * Keeps user validation and organization lookup behind a normal service abstraction.

 */
public interface UserAssignmentSupportService {

    void validateAssignable(UUID tenantId, UUID userId);

    Optional<UUID> resolveDepartmentId(UUID tenantId, UUID userId);

    Optional<UUID> resolveOrganizationId(UUID tenantId, UUID userId);
}
