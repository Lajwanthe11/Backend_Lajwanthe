package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.service.serviceImpl.RoleLookupServiceImpl;

import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.exception.RoleAssignmentValidationException;
import com.example.platformadmin.rbac.exception.RoleNotFoundException;
import com.example.platformadmin.rbac.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleLookupServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    private RoleLookupServiceImpl service;
    private UUID tenantId;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        service = new RoleLookupServiceImpl(roleRepository);
        tenantId = UUID.randomUUID();
        roleId = UUID.randomUUID();
    }

    @Test
    void getAssignableRole_shouldReturnActiveRole() {
        Role role = role(true, false);
        when(roleRepository.findByRoleIdAndTenantId(roleId, tenantId)).thenReturn(Optional.of(role));

        Role result = service.getAssignableRole(tenantId, roleId);

        assertSame(role, result);
    }

    @Test
    void getAssignableRole_shouldRejectInactiveRole() {
        Role role = role(false, false);
        when(roleRepository.findByRoleIdAndTenantId(roleId, tenantId)).thenReturn(Optional.of(role));

        RoleAssignmentValidationException ex = assertThrows(
                RoleAssignmentValidationException.class,
                () -> service.getAssignableRole(tenantId, roleId));

        assertEquals("Inactive roles cannot be assigned", ex.getMessage());
    }

    @Test
    void getAssignableRole_shouldRejectDeletedRole() {
        Role role = role(true, true);
        when(roleRepository.findByRoleIdAndTenantId(roleId, tenantId)).thenReturn(Optional.of(role));

        RoleAssignmentValidationException ex = assertThrows(
                RoleAssignmentValidationException.class,
                () -> service.getAssignableRole(tenantId, roleId));

        assertEquals("Deleted roles cannot be assigned", ex.getMessage());
    }

    @Test
    void getRole_shouldThrowWhenRoleDoesNotExistInTenant() {
        when(roleRepository.findByRoleIdAndTenantId(roleId, tenantId)).thenReturn(Optional.empty());

        RoleNotFoundException ex = assertThrows(
                RoleNotFoundException.class,
                () -> service.getRole(tenantId, roleId));

        assertTrue(ex.getMessage().contains(roleId.toString()));
    }

    @Test
    void getAssignableRoleByCode_shouldFindCaseInsensitiveRole() {
        Role role = role(true, false);
        role.setRoleCode("HR_MANAGER");
        when(roleRepository.findByTenantIdAndRoleCodeIgnoreCase(tenantId, "hr_manager"))
                .thenReturn(Optional.of(role));

        Role result = service.getAssignableRoleByCode(tenantId, "hr_manager");

        assertEquals("HR_MANAGER", result.getRoleCode());
    }

    private Role role(boolean active, boolean deleted) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setTenantId(tenantId);
        role.setRoleCode("HR_MANAGER");
        role.setRoleName("HR Manager");
        role.setActive(active);
        role.setDeleted(deleted);
        return role;
    }
}
