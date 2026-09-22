package com.example.rbac.audit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private UUID roleId;
    private UUID permissionId;
    private UUID changedBy;

    @BeforeEach
    void setUp() {

        roleId = UUID.randomUUID();
        permissionId = UUID.randomUUID();
        changedBy = UUID.randomUUID();

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

    // =========================================================
    // GRANT PERMISSION AUDIT
    // =========================================================

    @Test
    void grantPermission_shouldCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId))
                .thenReturn(Optional.empty());

        RolePermission savedRolePermission = new RolePermission();
        savedRolePermission.setActive(true);

        when(rolePermissionRepository.save(any(RolePermission.class)))
                .thenReturn(savedRolePermission);

        rolePermissionService.grantPermission(
                roleId,
                permissionId,
                changedBy
        );

        // Verify role-permission mapping was persisted.
        ArgumentCaptor<RolePermission> rolePermissionCaptor =
                ArgumentCaptor.forClass(RolePermission.class);

        verify(rolePermissionRepository)
                .save(rolePermissionCaptor.capture());

        RolePermission savedMapping =
                rolePermissionCaptor.getValue();

        assertTrue(savedMapping.isActive());

        // Verify audit record was persisted.
        ArgumentCaptor<RolePermissionAudit> auditCaptor =
                ArgumentCaptor.forClass(RolePermissionAudit.class);

        verify(auditRepository)
                .save(auditCaptor.capture());

        RolePermissionAudit audit =
                auditCaptor.getValue();

        /*
         * RolePermissionAudit uses String/UUID representations for
         * its identifier fields depending on the entity field type.
         *
         * String.valueOf(...) compares the actual UUID value without
         * making the test dependent on whether the getter returns
         * String or UUID.
         */
        assertEquals(
                roleId.toString(),
                String.valueOf(audit.getRoleId())
        );

        assertEquals(
                permissionId.toString(),
                String.valueOf(audit.getPermissionId())
        );

        assertEquals(
                changedBy.toString(),
                String.valueOf(audit.getChangedBy())
        );

        // New grant: false -> true.
        assertFalse(audit.isFromGranted());
        assertTrue(audit.isToGranted());

        assertNotNull(audit.getChangedAt());
    }

    // =========================================================
    // REVOKE PERMISSION AUDIT
    // =========================================================

    @Test
    void revokePermission_shouldCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        RolePermission existing = new RolePermission();
        existing.setActive(true);

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId))
                .thenReturn(Optional.of(existing));

        when(rolePermissionRepository.save(any(RolePermission.class)))
                .thenReturn(existing);

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                changedBy
        );

        // Revocation must deactivate the mapping.
        assertFalse(existing.isActive());

        verify(rolePermissionRepository)
                .save(existing);

        // Verify audit record was created.
        ArgumentCaptor<RolePermissionAudit> auditCaptor =
                ArgumentCaptor.forClass(RolePermissionAudit.class);

        verify(auditRepository)
                .save(auditCaptor.capture());

        RolePermissionAudit audit =
                auditCaptor.getValue();

        /*
         * Compare by UUID value rather than Java object type.
         */
        assertEquals(
                roleId.toString(),
                String.valueOf(audit.getRoleId())
        );

        assertEquals(
                permissionId.toString(),
                String.valueOf(audit.getPermissionId())
        );

        assertEquals(
                changedBy.toString(),
                String.valueOf(audit.getChangedBy())
        );

        // Revoke: true -> false.
        assertTrue(audit.isFromGranted());
        assertFalse(audit.isToGranted());

        assertNotNull(audit.getChangedAt());
    }

    // =========================================================
    // DUPLICATE / INACTIVE PERMISSION SCENARIOS
    // =========================================================

    @Test
    void duplicateActiveGrant_shouldNotCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        RolePermission existing = new RolePermission();
        existing.setActive(true);

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId))
                .thenReturn(Optional.of(existing));

        RolePermission result =
                rolePermissionService.grantPermission(
                        roleId,
                        permissionId,
                        changedBy
                );

        assertSame(existing, result);

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }

    @Test
    void revokeInactivePermission_shouldNotCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        RolePermission existing = new RolePermission();
        existing.setActive(false);

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId))
                .thenReturn(Optional.of(existing));

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                changedBy
        );

        assertFalse(existing.isActive());

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }

    @Test
    void revokeMissingPermissionMapping_shouldNotCreateAuditLog() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId))
                .thenReturn(Optional.empty());

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                changedBy
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }

    // =========================================================
    // INVALID ROLE / PERMISSION SCENARIOS
    // =========================================================

    @Test
    void grantInactivePermission_shouldRejectRequest() {

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
                        changedBy
                )
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }

    @Test
    void grantPermission_whenRoleDoesNotExist_shouldFail() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> rolePermissionService.grantPermission(
                        roleId,
                        permissionId,
                        changedBy
                )
        );

        verify(permissionRepository, never())
                .findById(any());

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }

    @Test
    void grantPermission_whenPermissionDoesNotExist_shouldFail() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> rolePermissionService.grantPermission(
                        roleId,
                        permissionId,
                        changedBy
                )
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }
}