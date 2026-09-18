package com.example.rbac.service;

import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionAuditRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;
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
 * These tests verify:
 * - Reading role permissions
 * - Granting permissions
 * - Revoking permissions
 * - Permission validation
 * - Super Admin system-permission protection
 * - Audit creation
 * - Tenant ID handling
 */
@ExtendWith(MockitoExtension.class)
class RolePermissionServiceTest {

        /*
         * Mock repository used for RolePermission database operations.
         */
        @Mock
        private RolePermissionRepository rolePermissionRepository;

        /*
         * Mock repository used for permission audit records.
         */
        @Mock
        private RolePermissionAuditRepository auditRepository;

        /*
         * Mock repository used to find and validate roles.
         */
        @Mock
        private RoleRepository roleRepository;

        /*
         * Mock repository used to find and validate permissions.
         */
        @Mock
        private PermissionRepository permissionRepository;

        /*
         * Service under test.
         */
        private RolePermissionService service;

        /*
         * Test IDs.
         *
         * The current Permission Matrix implementation uses UUIDs.
         */
        private UUID roleId;
        private UUID permissionId;
        private UUID userId;
        private UUID tenantId;

        /**
         * Create the service and initialize test IDs
         * before every test.
         */
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

                /*
                 * RolePermissionService reads the current tenant
                 * from TenantContext.
                 */
                com.example.common.tenant.TenantContext.setTenantId(
                                tenantId.toString());
        }

        /**
         * Clear TenantContext after every test so that
         * one test does not affect another test.
         */
        @AfterEach
        void tearDown() {

                com.example.common.tenant.TenantContext.clear();
        }

        /**
         * Verify that active permissions are returned
         * when the role exists.
         */
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

        /**
         * Verify that the service rejects the request
         * when the role does not exist.
         */
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
                                never()).findByRole_IdAndActiveTrue(any());
        }

        /**
         * Verify that an existing role-permission mapping
         * can be retrieved.
         */
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

        /**
         * Verify that null is returned when no
         * role-permission mapping exists.
         */
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
        }

        /**
         * Verify that a new permission assignment is created
         * and an audit record is written.
         */
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

                assertSame(
                                savedPermission,
                                result);

                /*
                 * Capture the RolePermission passed to the repository
                 * so that its fields can be verified.
                 */
                ArgumentCaptor<RolePermission> permissionCaptor = ArgumentCaptor.forClass(
                                RolePermission.class);

                verify(rolePermissionRepository)
                                .save(permissionCaptor.capture());

                RolePermission saved = permissionCaptor.getValue();

                assertSame(
                                role,
                                saved.getRole());

                assertSame(
                                permission,
                                saved.getPermission());

                assertEquals(
                                tenantId,
                                saved.getTenantId());

                assertEquals(
                                userId,
                                saved.getGrantedBy());

                assertNotNull(
                                saved.getGrantedAt());

                assertTrue(
                                saved.isActive());

                assertNull(
                                saved.getRevokedBy());

                assertNull(
                                saved.getRevokedAt());

                /*
                 * Granting a permission must create an audit record.
                 */
                verify(auditRepository)
                                .save(any());
        }

        /**
         * Verify that an already active permission is returned
         * without creating another database record or audit entry.
         */
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
                                never()).save(any(RolePermission.class));

                verify(
                                auditRepository,
                                never()).save(any());
        }

        /**
         * Verify that inactive permissions cannot be granted.
         */
        @Test
        void grantPermission_shouldRejectInactivePermission() {

                Role role = new Role();

                Permission permission = new Permission();

                when(roleRepository.findById(roleId))
                                .thenReturn(Optional.of(role));

                when(permissionRepository.findById(permissionId))
                                .thenReturn(Optional.of(permission));

                permission.setActive(false);

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
                                never()).save(any());

                verify(
                                auditRepository,
                                never()).save(any());
        }

        /**
         * Verify that granting a permission fails
         * when the role does not exist.
         */
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
                                never()).findById(any());
        }

        /**
         * Verify that granting a permission fails
         * when the permission does not exist.
         */
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
                                never()).save(any());
        }

        /**
         * Verify that an active permission is deactivated
         * when it is revoked and an audit record is created.
         */
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

                assertEquals(
                                userId,
                                rolePermission.getRevokedBy());

                assertNotNull(
                                rolePermission.getRevokedAt());

                verify(rolePermissionRepository)
                                .save(rolePermission);

                verify(auditRepository)
                                .save(any());
        }

        /**
         * Verify that revoking a permission that is not
         * currently granted does not create a database
         * or audit operation.
         */
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
                                never()).save(any());

                verify(
                                auditRepository,
                                never()).save(any());
        }

        /**
         * Verify that a Super Admin cannot revoke
         * a system permission.
         */
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
                                never()).save(any());

                verify(
                                auditRepository,
                                never()).save(any());
        }

        /**
         * Verify that system permissions can be revoked
         * from a normal non-Super-Admin role.
         */
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

                verify(rolePermissionRepository)
                                .save(rolePermission);

                verify(auditRepository)
                                .save(any());
        }
}
