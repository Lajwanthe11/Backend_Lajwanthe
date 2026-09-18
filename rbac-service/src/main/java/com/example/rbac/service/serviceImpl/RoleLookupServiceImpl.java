package com.example.rbac.service.serviceImpl;

import com.example.rbac.entity.Role;
import com.example.rbac.exception.RoleAssignmentValidationException;
import com.example.rbac.exception.RoleNotFoundException;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.service.RoleLookupService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RoleLookupServiceImpl implements RoleLookupService {

    private final RoleRepository roleRepository;

    public RoleLookupServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getAssignableRole(UUID tenantId, UUID roleId) {
        Role role = getRole(tenantId, roleId);
        validateAssignable(role);
        return role;
    }

    @Override
    public Role getAssignableRoleByCode(UUID tenantId, String roleCode) {
        Role role = roleRepository.findByTenantIdAndRoleCodeIgnoreCase(tenantId, roleCode)
                .orElseThrow(() -> new RoleNotFoundException("Role not found for code: " + roleCode));
        validateAssignable(role);
        return role;
    }

    @Override
    public Role getRole(UUID tenantId, UUID roleId) {
        return roleRepository.findByRoleIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RoleNotFoundException(
                        "Role not found in the current tenant: " + roleId));
    }

    private void validateAssignable(Role role) {
        if (role.isDeleted()) {
            throw new RoleAssignmentValidationException("Deleted roles cannot be assigned");
        }
        if (!role.isActive()) {
            throw new RoleAssignmentValidationException("Inactive roles cannot be assigned");
        }
    }
}
