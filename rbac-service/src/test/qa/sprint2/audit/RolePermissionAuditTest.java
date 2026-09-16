package com.example.qa.sprint2.audit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.common.tenant.TenantContext;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.entity.RolePermissionAudit;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionAuditRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.service.RolePermissionService;

@ExtendWith(MockitoExtension.class)
class RolePermissionAuditTest {

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RolePermissionAuditRepository auditRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RolePermissionService rolePermissionService;

    private Role role;
    private Permission permission;

    private Long roleId;
    private UUID permissionId;

    @BeforeEach
    void setUp() {

        roleId = 1L;
        permissionId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setRoleCode("HR_MANAGER");
        role.setRoleName("HR Manager");

        permission = new Permission();
        permission.setPermissionId(permissionId);
        permission.setPermissionCode("USER_READ");
        permission.setActive(true);
        permission.setSystem(false);
    }

    @Test
    void grantPermission_shouldCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId, permissionId))
                .thenReturn(Optional.empty());

        RolePermission savedRolePermission = new RolePermission();

        when(rolePermissionRepository.save(any(RolePermission.class)))
                .thenReturn(savedRolePermission);

        rolePermissionService.grantPermission(
                roleId,
                permissionId,
                "admin-user"
        );

        ArgumentCaptor<RolePermissionAudit> captor =
                ArgumentCaptor.forClass(RolePermissionAudit.class);

        verify(auditRepository).save(captor.capture());

        RolePermissionAudit audit = captor.getValue();

        assertEquals(roleId, audit.getRoleId());
        assertEquals(permissionId, audit.getPermissionId());
        assertEquals("admin-user", audit.getChangedBy());

        assertFalse(audit.isFromGranted());
        assertTrue(audit.isToGranted());

        assertNotNull(audit.getChangedAt());
    }

    @Test
    void revokePermission_shouldCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        RolePermission rolePermission =
                new RolePermission();

        rolePermission.setActive(true);

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId, permissionId))
                .thenReturn(Optional.of(rolePermission));

        when(rolePermissionRepository.save(any(RolePermission.class)))
                .thenReturn(rolePermission);

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                "admin-user"
        );

        ArgumentCaptor<RolePermissionAudit> captor =
                ArgumentCaptor.forClass(RolePermissionAudit.class);

        verify(auditRepository).save(captor.capture());

        RolePermissionAudit audit = captor.getValue();

        assertEquals(roleId, audit.getRoleId());
        assertEquals(permissionId, audit.getPermissionId());
        assertEquals("admin-user", audit.getChangedBy());

        assertTrue(audit.isFromGranted());
        assertFalse(audit.isToGranted());

        assertNotNull(audit.getChangedAt());
    }

    @Test
    void duplicateActiveGrant_shouldNotCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        RolePermission existing =
                new RolePermission();

        existing.setActive(true);

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId, permissionId))
                .thenReturn(Optional.of(existing));

        RolePermission result =
                rolePermissionService.grantPermission(
                        roleId,
                        permissionId,
                        "admin-user"
                );

        assertSame(existing, result);

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }

    @Test
    void revokeInactivePermission_shouldNotCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        RolePermission existing =
                new RolePermission();

        existing.setActive(false);

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId, permissionId))
                .thenReturn(Optional.of(existing));

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                "admin-user"
        );

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));
    }

    @Test
    void revokeMissingPermissionMapping_shouldNotCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId, permissionId))
                .thenReturn(Optional.empty());

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                "admin-user"
        );

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }

    @Test
    void grantInactivePermission_shouldNotCreateAuditLog() {

        permission.setActive(false);

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        assertThrows(
                IllegalArgumentException.class,
                () -> rolePermissionService.grantPermission(
                        roleId,
                        permissionId,
                        "admin-user"
                )
        );

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }
}