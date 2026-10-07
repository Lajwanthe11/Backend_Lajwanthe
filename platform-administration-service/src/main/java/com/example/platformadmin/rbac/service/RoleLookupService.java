package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.entity.Role;

import java.util.UUID;

public interface RoleLookupService {
    Role getAssignableRole(UUID tenantId, UUID roleId);
    Role getAssignableRoleByCode(UUID tenantId, String roleCode);
    Role getRole(UUID tenantId, UUID roleId);
}
