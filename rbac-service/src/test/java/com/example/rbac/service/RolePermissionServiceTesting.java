package com.example.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionAuditRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTesting {

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
    private RolePermission rolePermission;

    private UUID roleId;
    private UUID permissionId;
    private UUID changedBy;

    @BeforeEach
    void setUp() {

        roleId = UUID.randomUUID();
        permissionId = UUID.randomUUID();
        changedBy = UUID.randomUUID();

        role = mock(Role.class);
        permission = mock(Permission.class);
        rolePermission = mock(RolePermission.class);

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.of(permission));
    }

    @Test
    void getPermissionsByRole_shouldReturnActivePermissions() {

        List<RolePermission> permissions =
                List.of(rolePermission);

        when(rolePermissionRepository
                .findByRole_IdAndActiveTrue(roleId))
                .thenReturn(permissions);

        List<RolePermission> result =
                rolePermissionService.getPermissionsByRole(roleId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(rolePermission, result.get(0));

        verify(roleRepository)
                .findById(roleId);

        verify(rolePermissionRepository)
                .findByRole_IdAndActiveTrue(roleId);
    }

    @Test
    void getPermissionsByRole_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> rolePermissionService
                                .getPermissionsByRole(roleId)
                );

        assertEquals(
                "Role not found",
                exception.getMessage()
        );

        verify(rolePermissionRepository, never())
                .findByRole_IdAndActiveTrue(
                        any(UUID.class)
                );
    }

    @Test
    void getRolePermission_shouldReturnExistingMapping() {

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId
                ))
                .thenReturn(Optional.of(rolePermission));

        RolePermission result =
                rolePermissionService.getRolePermission(
                        roleId,
                        permissionId
                );

        assertSame(
                rolePermission,
                result
        );

        verify(rolePermissionRepository)
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId
                );
    }

    @Test
    void getRolePermission_shouldReturnNullWhenMappingDoesNotExist() {

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId
                ))
                .thenReturn(Optional.empty());

        RolePermission result =
                rolePermissionService.getRolePermission(
                        roleId,
                        permissionId
                );

        assertNull(result);
    }

    @Test
    void grantPermission_shouldCreateNewMapping() {

        when(permission.isActive())
                .thenReturn(true);

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId
                ))
                .thenReturn(Optional.empty());

        when(rolePermissionRepository
                .save(any(RolePermission.class)))
                .thenReturn(rolePermission);

        RolePermission result =
                rolePermissionService.grantPermission(
                        roleId,
                        permissionId,
                        changedBy
                );

        assertSame(
                rolePermission,
                result
        );

        verify(rolePermissionRepository)
                .save(any(RolePermission.class));

        verify(auditRepository)
                .save(any());
    }

    @Test
    void grantPermission_shouldReturnExistingActiveMapping() {

        when(permission.isActive())
                .thenReturn(true);

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId
                ))
                .thenReturn(Optional.of(rolePermission));

        when(rolePermission.isActive())
                .thenReturn(true);

        RolePermission result =
                rolePermissionService.grantPermission(
                        roleId,
                        permissionId,
                        changedBy
                );

        assertSame(
                rolePermission,
                result
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any());
    }

    @Test
    void grantPermission_shouldRejectInactivePermission() {

        when(permission.isActive())
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> rolePermissionService
                                .grantPermission(
                                        roleId,
                                        permissionId,
                                        changedBy
                                )
                );

        assertEquals(
                "Inactive permission cannot be granted",
                exception.getMessage()
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any());
    }

    @Test
    void grantPermission_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> rolePermissionService
                                .grantPermission(
                                        roleId,
                                        permissionId,
                                        changedBy
                                )
                );

        assertEquals(
                "Role not found",
                exception.getMessage()
        );

        verify(permissionRepository, never())
                .findById(any(UUID.class));

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any());
    }

    @Test
    void grantPermission_shouldThrowWhenPermissionDoesNotExist() {

        when(permissionRepository.findById(permissionId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> rolePermissionService
                                .grantPermission(
                                        roleId,
                                        permissionId,
                                        changedBy
                                )
                );

        assertEquals(
                "Permission not found",
                exception.getMessage()
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any());
    }

    @Test
    void revokePermission_shouldDeactivateActivePermission() {

        when(permission.isSystem())
                .thenReturn(false);

        when(role.getRoleCode())
                .thenReturn("HR_MANAGER");

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId
                ))
                .thenReturn(Optional.of(rolePermission));

        when(rolePermission.isActive())
                .thenReturn(true);

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                changedBy
        );

        verify(rolePermission)
                .setActive(false);

        verify(rolePermission)
                .setRevokedBy(changedBy);

        verify(rolePermission)
                .setRevokedAt(any());

        verify(rolePermissionRepository)
                .save(rolePermission);

        verify(auditRepository)
                .save(any());
    }

    @Test
    void revokePermission_shouldDoNothingWhenMappingDoesNotExist() {

        when(permission.isSystem())
                .thenReturn(false);

        when(role.getRoleCode())
                .thenReturn("HR_MANAGER");

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId
                ))
                .thenReturn(Optional.empty());

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                changedBy
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any());
    }

    @Test
    void revokePermission_shouldDoNothingWhenMappingAlreadyInactive() {

        when(permission.isSystem())
                .thenReturn(false);

        when(role.getRoleCode())
                .thenReturn("HR_MANAGER");

        when(rolePermissionRepository
                .findByRole_IdAndPermission_PermissionId(
                        roleId,
                        permissionId
                ))
                .thenReturn(Optional.of(rolePermission));

        when(rolePermission.isActive())
                .thenReturn(false);

        rolePermissionService.revokePermission(
                roleId,
                permissionId,
                changedBy
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any());
    }

    @Test
    void revokePermission_shouldPreventSystemPermissionRemovalFromSuperAdmin() {

        when(role.getRoleCode())
                .thenReturn("SUPER_ADMIN");

        when(permission.isSystem())
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> rolePermissionService
                                .revokePermission(
                                        roleId,
                                        permissionId,
                                        changedBy
                                )
                );

        assertEquals(
                "System permissions cannot be revoked from Super Admin",
                exception.getMessage()
        );

        verify(rolePermissionRepository, never())
                .save(any(RolePermission.class));

        verify(auditRepository, never())
                .save(any());
    }
}