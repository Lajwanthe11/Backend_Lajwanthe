package com.example.rbac.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class RolePermissionAuditTesting {

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

        // Create UUID-based test identifiers to match the RBAC entities
        // and service/repository method signatures.
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

        // The role must exist before a permission can be granted.
        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        // The permission must exist and be active.
        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));

        // No existing mapping means this is a new permission grant.
        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId))
                .thenReturn(Optional.empty());

        RolePermission savedRolePermission =
                new RolePermission();

        savedRolePermission.setActive(true);

        when(rolePermissionRepository.save(any(RolePermission.class)))
                .thenReturn(savedRolePermission);

        rolePermissionService.grantPermission(
                roleId,
                permissionId,
                changedBy
        );

        // Verify that a new role-permission mapping was persisted.
        ArgumentCaptor<RolePermission> rolePermissionCaptor =
                ArgumentCaptor.forClass(RolePermission.class);

        verify(rolePermissionRepository)
                .save(rolePermissionCaptor.capture());

        RolePermission savedMapping =
                rolePermissionCaptor.getValue();

        assertTrue(savedMapping.isActive());

        // Verify that the permission change was also recorded in
        // the audit table.
        ArgumentCaptor<RolePermissionAudit> auditCaptor =
                ArgumentCaptor.forClass(RolePermissionAudit.class);

        verify(auditRepository)
                .save(auditCaptor.capture());

        RolePermissionAudit audit =
                auditCaptor.getValue();

        assertEquals(roleId, audit.getRoleId());
        assertEquals(permissionId, audit.getPermissionId());
        assertEquals(changedBy, audit.getChangedBy());

        // A new grant changes the permission state from false to true.
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

        // Existing active mapping represents a currently granted permission.
        RolePermission existing =
                new RolePermission();

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

        // Revocation must deactivate the existing mapping.
        assertFalse(existing.isActive());

        verify(rolePermissionRepository)
                .save(existing);

        // Verify that the revoke operation created an audit record.
        ArgumentCaptor<RolePermissionAudit> auditCaptor =
                ArgumentCaptor.forClass(RolePermissionAudit.class);

        verify(auditRepository)
                .save(auditCaptor.capture());

        RolePermissionAudit audit =
                auditCaptor.getValue();

        assertEquals(roleId, audit.getRoleId());
        assertEquals(permissionId, audit.getPermissionId());
        assertEquals(changedBy, audit.getChangedBy());

        // A revoke changes the permission state from true to false.
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

        // An active mapping already exists, so another grant should
        // not create a new mapping or audit entry.
        RolePermission existing =
                new RolePermission();

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

        // An inactive mapping has already been revoked, so another
        // revoke operation should not persist anything.
        RolePermission existing =
                new RolePermission();

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

        // No role-permission mapping exists, so there is nothing to revoke.
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

        // An inactive permission must not be granted to a role.
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

        // Failed authorization changes must not persist either
        // the mapping or its audit entry.
        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }

    @Test
    void grantPermission_whenRoleDoesNotExist_shouldFail() {

        // The operation must fail when the requested role cannot be found.
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

        // Permission lookup should not happen after the role lookup fails.
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

        // The operation must fail when the requested permission cannot
        // be found.
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

        // A failed permission lookup must not modify the mapping
        // or create an audit record.
        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any(RolePermissionAudit.class));
    }
}