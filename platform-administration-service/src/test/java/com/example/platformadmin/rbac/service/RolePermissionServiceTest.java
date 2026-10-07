package com.example.platformadmin.rbac.service;

import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.entity.Permission;
import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.entity.RolePermission;
import com.example.platformadmin.rbac.repository.PermissionRepository;
import com.example.platformadmin.rbac.repository.RolePermissionAuditRepository;
import com.example.platformadmin.rbac.repository.RolePermissionRepository;
import com.example.platformadmin.rbac.repository.RoleRepository;
import com.example.platformadmin.rbac.service.serviceImpl.RolePermissionService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RolePermissionService.
 *
 * Covers:
 * - Reading role permissions
 * - Getting a specific role-permission mapping
 * - Granting permissions
 * - Re-granting active permissions
 * - Rejecting inactive permissions
 * - Revoking permissions
 * - Super Admin system-permission protection
 * - Audit creation
 * - Tenant ID handling
 */
@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {

        @Mock
        private RolePermissionRepository rolePermissionRepository;

        @Mock
        private RolePermissionAuditRepository auditRepository;

        @Mock
        private RoleRepository roleRepository;

        @Mock
        private PermissionRepository permissionRepository;

        private RolePermissionService service;

        private UUID roleId;
        private UUID permissionId;
        private UUID userId;
        private UUID tenantId;

        @BeforeEach
        void setUp() {

                service = new RolePermissionService(
                                rolePermissionRepository,
                                auditRepository,
                                roleRepository,
                                permissionRepository);

                roleId = UUID.randomUUID();
                permissionId = UUID.randomUUID();
                userId = UUID.randomUUID();
                tenantId = UUID.randomUUID();

                TenantContext.setTenantId(tenantId.toString());
        }

        @AfterEach
        void tearDown() {
                TenantContext.clear();
        }

        // -------------------------------------------------------------------------
        // getPermissionsByRole()
        // -------------------------------------------------------------------------

        @Test
        void getPermissionsByRole_shouldReturnActivePermissions() {

                Role role = new Role();
                RolePermission rolePermission = new RolePermission();

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(rolePermissionRepository.findByRole_IdAndActiveTrue(roleId))
                                .thenReturn(List.of(rolePermission));

                List<RolePermission> result = service.getPermissionsByRole(roleId);

                assertNotNull(result);
                assertEquals(1, result.size());
                assertSame(rolePermission, result.get(0));

                verify(roleRepository).findById(roleId);

                verify(rolePermissionRepository)
                                .findByRole_IdAndActiveTrue(roleId);
        }

        @Test
        void getPermissionsByRole_shouldThrowWhenRoleNotFound() {

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.empty());

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.getPermissionsByRole(roleId));

                assertEquals(
                                "Role not found",
                                exception.getMessage());

                verify(roleRepository).findById(roleId);

                verify(
                                rolePermissionRepository,
                                never())
                                .findByRole_IdAndActiveTrue(any());
        }

        // -------------------------------------------------------------------------
        // getRolePermission()
        // -------------------------------------------------------------------------

        @Test
        void getRolePermission_shouldReturnPermission() {

                RolePermission rolePermission = new RolePermission();

                when(
                                rolePermissionRepository
                                                .findByRole_IdAndPermission_PermissionId(
                                                                roleId,
                                                                permissionId))
                                .thenReturn(Optional.of(rolePermission));

                RolePermission result = service.getRolePermission(
                                roleId,
                                permissionId);

                assertSame(
                                rolePermission,
                                result);

                verify(rolePermissionRepository)
                                .findByRole_IdAndPermission_PermissionId(
                                                roleId,
                                                permissionId);
        }

        @Test
        void getRolePermission_shouldReturnNullWhenNotFound() {

                when(
                                rolePermissionRepository
                                                .findByRole_IdAndPermission_PermissionId(
                                                                roleId,
                                                                permissionId))
                                .thenReturn(Optional.empty());

                RolePermission result = service.getRolePermission(
                                roleId,
                                permissionId);

                assertNull(result);

                verify(rolePermissionRepository)
                                .findByRole_IdAndPermission_PermissionId(
                                                roleId,
                                                permissionId);
        }

        // -------------------------------------------------------------------------
        // grantPermission()
        // -------------------------------------------------------------------------

        @Test
        void grantPermission_shouldCreatePermissionAndAudit() {

                Role role = new Role();
                Permission permission = new Permission();

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.of(permission));

                when(
                                rolePermissionRepository
                                                .findByRole_IdAndPermission_PermissionId(
                                                                roleId,
                                                                permissionId))
                                .thenReturn(Optional.empty());

                RolePermission savedPermission = new RolePermission();

                when(
                                rolePermissionRepository
                                                .save(any(RolePermission.class)))
                                .thenReturn(savedPermission);

                RolePermission result = service.grantPermission(
                                roleId,
                                permissionId,
                                userId);

                // Returned object must be the repository result.
                assertSame(
                                savedPermission,
                                result);

                // Capture the entity passed to save().
                ArgumentCaptor<RolePermission> permissionCaptor = ArgumentCaptor.forClass(RolePermission.class);

                verify(rolePermissionRepository)
                                .save(permissionCaptor.capture());

                RolePermission saved = permissionCaptor.getValue();

                // Correct role.
                assertSame(
                                role,
                                saved.getRole());

                // Correct permission.
                assertSame(
                                permission,
                                saved.getPermission());

                /*
                 * IMPORTANT:
                 *
                 * RolePermission.tenantId is String in the current
                 * developer implementation.
                 *
                 * RolePermissionService does:
                 *
                 * rolePermission.setTenantId(getTenantId().toString());
                 *
                 * Therefore compare String with String.
                 */
                assertEquals(
                                tenantId.toString(),
                                saved.getTenantId());

                /*
                 * grantedBy is also String in the current implementation.
                 */
                assertEquals(
                                userId.toString(),
                                saved.getGrantedBy());

                assertNotNull(
                                saved.getGrantedAt());

                assertTrue(
                                saved.isActive());

                assertNull(
                                saved.getRevokedBy());

                assertNull(
                                saved.getRevokedAt());

                // Audit must be created.
                verify(auditRepository)
                                .save(any());
        }

        @Test
        void grantPermission_shouldReturnExistingActivePermission() {

                Role role = new Role();
                Permission permission = new Permission();

                RolePermission existing = new RolePermission();

                existing.setActive(true);

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.of(permission));

                when(
                                rolePermissionRepository
                                                .findByRole_IdAndPermission_PermissionId(
                                                                roleId,
                                                                permissionId))
                                .thenReturn(Optional.of(existing));

                RolePermission result = service.grantPermission(
                                roleId,
                                permissionId,
                                userId);

                assertSame(
                                existing,
                                result);

                verify(
                                rolePermissionRepository,
                                never())
                                .save(any(RolePermission.class));

                verify(
                                auditRepository,
                                never())
                                .save(any());
        }

        @Test
        void grantPermission_shouldRejectInactivePermission() {

                Role role = new Role();
                Permission permission = new Permission();

                permission.setActive(false);

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.of(permission));

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.grantPermission(
                                                roleId,
                                                permissionId,
                                                userId));

                assertEquals(
                                "Inactive permission cannot be granted",
                                exception.getMessage());

                verify(
                                rolePermissionRepository,
                                never())
                                .save(any());

                verify(
                                auditRepository,
                                never())
                                .save(any());
        }

        @Test
        void grantPermission_shouldThrowWhenRoleNotFound() {

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.empty());

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.grantPermission(
                                                roleId,
                                                permissionId,
                                                userId));

                assertEquals(
                                "Role not found",
                                exception.getMessage());

                verify(
                                permissionRepository,
                                never())
                                .findById(any());

                verify(
                                rolePermissionRepository,
                                never())
                                .save(any());

                verify(
                                auditRepository,
                                never())
                                .save(any());
        }

        @Test
        void grantPermission_shouldThrowWhenPermissionNotFound() {

                Role role = new Role();

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.empty());

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.grantPermission(
                                                roleId,
                                                permissionId,
                                                userId));

                assertEquals(
                                "Permission not found",
                                exception.getMessage());

                verify(
                                rolePermissionRepository,
                                never())
                                .save(any());

                verify(
                                auditRepository,
                                never())
                                .save(any());
        }

        // -------------------------------------------------------------------------
        // revokePermission()
        // -------------------------------------------------------------------------

        @Test
        void revokePermission_shouldDeactivatePermissionAndCreateAudit() {

                Role role = new Role();
                Permission permission = new Permission();

                RolePermission rolePermission = new RolePermission();

                rolePermission.setActive(true);

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.of(permission));

                when(
                                rolePermissionRepository
                                                .findByRole_IdAndPermission_PermissionId(
                                                                roleId,
                                                                permissionId))
                                .thenReturn(Optional.of(rolePermission));

                service.revokePermission(
                                roleId,
                                permissionId,
                                userId);

                assertFalse(
                                rolePermission.isActive());

                /*
                 * revokedBy is String in the current implementation.
                 */
                assertEquals(
                                userId.toString(),
                                rolePermission.getRevokedBy());

                assertNotNull(
                                rolePermission.getRevokedAt());

                verify(rolePermissionRepository)
                                .save(rolePermission);

                verify(auditRepository)
                                .save(any());
        }

        @Test
        void revokePermission_shouldDoNothingWhenPermissionNotGranted() {

                Role role = new Role();
                Permission permission = new Permission();

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.of(permission));

                when(
                                rolePermissionRepository
                                                .findByRole_IdAndPermission_PermissionId(
                                                                roleId,
                                                                permissionId))
                                .thenReturn(Optional.empty());

                service.revokePermission(
                                roleId,
                                permissionId,
                                userId);

                verify(
                                rolePermissionRepository,
                                never())
                                .save(any());

                verify(
                                auditRepository,
                                never())
                                .save(any());
        }

        @Test
        void revokePermission_shouldRejectSystemPermissionForSuperAdmin() {

                Role role = new Role();
                Permission permission = new Permission();

                role.setRoleCode("SUPER_ADMIN");
                permission.setSystem(true);

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.of(permission));

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.revokePermission(
                                                roleId,
                                                permissionId,
                                                userId));

                assertEquals(
                                "System permissions cannot be revoked from Super Admin",
                                exception.getMessage());

                verify(
                                rolePermissionRepository,
                                never())
                                .save(any());

                verify(
                                auditRepository,
                                never())
                                .save(any());
        }

        @Test
        void revokePermission_shouldAllowSystemPermissionForNonSuperAdmin() {

                Role role = new Role();
                Permission permission = new Permission();

                RolePermission rolePermission = new RolePermission();

                rolePermission.setActive(true);

                role.setRoleCode("HR");
                permission.setSystem(true);

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.of(permission));

                when(
                                rolePermissionRepository
                                                .findByRole_IdAndPermission_PermissionId(
                                                                roleId,
                                                                permissionId))
                                .thenReturn(Optional.of(rolePermission));

                service.revokePermission(
                                roleId,
                                permissionId,
                                userId);

                assertFalse(
                                rolePermission.isActive());

                assertEquals(
                                userId.toString(),
                                rolePermission.getRevokedBy());

                assertNotNull(
                                rolePermission.getRevokedAt());

                verify(rolePermissionRepository)
                                .save(rolePermission);

                verify(auditRepository)
                                .save(any());
        }
}
