package com.example.rbac.service;

import com.example.rbac.entity.Role;

import java.util.UUID;

public interface RoleLookupService {
    Role getAssignableRole(UUID tenantId, UUID roleId);
    Role getAssignableRoleByCode(UUID tenantId, String roleCode);
    Role getRole(UUID tenantId, UUID roleId);
}
