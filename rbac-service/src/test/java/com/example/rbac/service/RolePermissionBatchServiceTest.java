package com.example.rbac.service;

import com.example.rbac.dto.BatchPermissionUpdateRequest;
import com.example.rbac.dto.BatchPermissionUpdateResponse;
import com.example.rbac.dto.PermissionGrantRequest;
import com.example.rbac.entity.Role;
import com.example.rbac.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RolePermissionBatchService.
 *
 * These tests verify:
 * - Granting permissions in batch
 * - Revoking permissions in batch
 * - Handling mixed grant and revoke operations
 * - Handling an empty permission list
 * - Handling a missing role
 * - Updating the role audit information
 * - Processing all permissions in the request
 */
@ExtendWith(MockitoExtension.class)
class RolePermissionBatchServiceTest {

        /*
         * Mock repository used to find and update the role.
         */
        @Mock
        private RoleRepository roleRepository;

        /*
         * Mock service used to perform the actual
         * grant and revoke operations.
         */
        @Mock
        private RolePermissionService rolePermissionService;

        /*
         * Mockito creates the service and injects the
         * required mocked dependencies.
         */
        @InjectMocks
        private RolePermissionBatchService rolePermissionBatchService;

        /*
         * UUIDs used throughout the tests.
         *
         * The current RBAC implementation uses UUID
         * identifiers for roles and permissions.
         */
        private UUID roleId;
        private UUID userId;
        private UUID permissionId1;
        private UUID permissionId2;

        /*
         * Test role object used by the service.
         */
        private Role role;

        /**
         * Prepare fresh test data before every test.
         */
        @BeforeEach
        void setUp() {

                roleId = UUID.randomUUID();
                userId = UUID.randomUUID();
                permissionId1 = UUID.randomUUID();
                permissionId2 = UUID.randomUUID();

                role = new Role();
        }

        /**
         * Verify that a permission with granted=true
         * calls the grant operation.
         */
        @Test
        void shouldUpdateGrantedPermissionsSuccessfully() {

                PermissionGrantRequest permission = new PermissionGrantRequest();

                permission.setPermissionId(permissionId1);
                permission.setGranted(true);

                BatchPermissionUpdateRequest request = new BatchPermissionUpdateRequest();

                request.setPermissions(
                                List.of(permission));

                /*
                 * The role must exist before batch processing starts.
                 */
                when(roleRepository.findById(roleId))
                                .thenReturn(java.util.Optional.of(role));

                BatchPermissionUpdateResponse response = rolePermissionBatchService.updatePermissions(
                                roleId,
                                request,
                                userId);

                /*
                 * Verify that the permission is granted.
                 */
                verify(rolePermissionService)
                                .grantPermission(
                                                roleId,
                                                permissionId1,
                                                userId);

                /*
                 * No revoke operation should happen
                 * for a granted permission.
                 */
                verify(
                                rolePermissionService,
                                never()).revokePermission(
                                                any(),
                                                any(),
                                                any());

                /*
                 * The role is saved after processing the batch.
                 */
                verify(roleRepository)
                                .saveAndFlush(role);

                /*
                 * Verify the batch response.
                 */
                assertEquals(
                                roleId,
                                response.getRoleId());

                assertEquals(
                                1,
                                response.getUpdatedCount());

                assertEquals(
                                "Permissions updated successfully",
                                response.getMessage());
        }

        /**
         * Verify that a permission with granted=false
         * calls the revoke operation.
         */
        @Test
        void shouldUpdateRevokedPermissionsSuccessfully() {

                PermissionGrantRequest permission = new PermissionGrantRequest();

                permission.setPermissionId(permissionId1);
                permission.setGranted(false);

                BatchPermissionUpdateRequest request = new BatchPermissionUpdateRequest();

                request.setPermissions(
                                List.of(permission));

                when(roleRepository.findById(roleId))
                                .thenReturn(java.util.Optional.of(role));

                BatchPermissionUpdateResponse response = rolePermissionBatchService.updatePermissions(
                                roleId,
                                request,
                                userId);

                /*
                 * Verify that the permission is revoked.
                 */
                verify(rolePermissionService)
                                .revokePermission(
                                                roleId,
                                                permissionId1,
                                                userId);

                /*
                 * No grant operation should happen.
                 */
                verify(
                                rolePermissionService,
                                never()).grantPermission(
                                                any(),
                                                any(),
                                                any());

                verify(roleRepository)
                                .saveAndFlush(role);

                assertEquals(
                                roleId,
                                response.getRoleId());

                assertEquals(
                                1,
                                response.getUpdatedCount());

                assertEquals(
                                "Permissions updated successfully",
                                response.getMessage());
        }

        /**
         * Verify that a batch can contain both
         * grant and revoke operations.
         */
        @Test
        void shouldHandleMixedGrantAndRevokePermissions() {

                PermissionGrantRequest grantPermission = new PermissionGrantRequest();

                grantPermission.setPermissionId(permissionId1);
                grantPermission.setGranted(true);

                PermissionGrantRequest revokePermission = new PermissionGrantRequest();

                revokePermission.setPermissionId(permissionId2);
                revokePermission.setGranted(false);

                BatchPermissionUpdateRequest request = new BatchPermissionUpdateRequest();

                request.setPermissions(
                                List.of(
                                                grantPermission,
                                                revokePermission));

                when(roleRepository.findById(roleId))
                                .thenReturn(java.util.Optional.of(role));

                BatchPermissionUpdateResponse response = rolePermissionBatchService.updatePermissions(
                                roleId,
                                request,
                                userId);

                /*
                 * First permission should be granted.
                 */
                verify(rolePermissionService)
                                .grantPermission(
                                                roleId,
                                                permissionId1,
                                                userId);

                /*
                 * Second permission should be revoked.
                 */
                verify(rolePermissionService)
                                .revokePermission(
                                                roleId,
                                                permissionId2,
                                                userId);

                verify(roleRepository)
                                .saveAndFlush(role);

                assertEquals(
                                roleId,
                                response.getRoleId());

                assertEquals(
                                2,
                                response.getUpdatedCount());
        }

        /**
         * Verify that an empty permission list is handled
         * without calling grant or revoke operations.
         */
        @Test
        void shouldReturnZeroWhenPermissionListIsEmpty() {

                BatchPermissionUpdateRequest request = new BatchPermissionUpdateRequest();

                request.setPermissions(
                                List.of());

                when(roleRepository.findById(roleId))
                                .thenReturn(java.util.Optional.of(role));

                BatchPermissionUpdateResponse response = rolePermissionBatchService.updatePermissions(
                                roleId,
                                request,
                                userId);

                /*
                 * No permission operations should occur.
                 */
                verify(
                                rolePermissionService,
                                never()).grantPermission(
                                                any(),
                                                any(),
                                                any());

                verify(
                                rolePermissionService,
                                never()).revokePermission(
                                                any(),
                                                any(),
                                                any());

                /*
                 * The role is still saved after processing
                 * the empty batch.
                 */
                verify(roleRepository)
                                .saveAndFlush(role);

                assertEquals(
                                roleId,
                                response.getRoleId());

                assertEquals(
                                0,
                                response.getUpdatedCount());

                assertEquals(
                                "Permissions updated successfully",
                                response.getMessage());
        }

        /**
         * Verify that the service throws an exception
         * when the requested role does not exist.
         */
        @Test
        void shouldThrowExceptionWhenRoleNotFound() {

                BatchPermissionUpdateRequest request = new BatchPermissionUpdateRequest();

                request.setPermissions(
                                List.of());

                when(roleRepository.findById(roleId))
                                .thenReturn(java.util.Optional.empty());

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> rolePermissionBatchService
                                                .updatePermissions(
                                                                roleId,
                                                                request,
                                                                userId));

                assertEquals(
                                "Role not found",
                                exception.getMessage());

                /*
                 * The role must not be saved when it does not exist.
                 */
                verify(
                                roleRepository,
                                never()).saveAndFlush(any());

                /*
                 * Permission processing must not start
                 * when the role is missing.
                 */
                verifyNoInteractions(
                                rolePermissionService);
        }

        /**
         * Verify that the authenticated user's ID is stored
         * as the role's updatedBy value before saving.
         */
        @Test
        void shouldSetUpdatedByBeforeSaving() {

                PermissionGrantRequest permission = new PermissionGrantRequest();

                permission.setPermissionId(permissionId1);
                permission.setGranted(true);

                BatchPermissionUpdateRequest request = new BatchPermissionUpdateRequest();

                request.setPermissions(
                                List.of(permission));

                when(roleRepository.findById(roleId))
                                .thenReturn(java.util.Optional.of(role));

                rolePermissionBatchService.updatePermissions(
                                roleId,
                                request,
                                userId);

                /*
                 * Verify that the user performing the batch update
                 * is recorded on the role.
                 */
                assertEquals(
                                userId,
                                role.getUpdatedBy());

                verify(roleRepository)
                                .saveAndFlush(role);
        }

        /**
         * Verify that every permission in the request
         * is processed.
         */
        @Test
        void shouldProcessAllPermissions() {

                PermissionGrantRequest permission1 = new PermissionGrantRequest();

                permission1.setPermissionId(permissionId1);
                permission1.setGranted(true);

                PermissionGrantRequest permission2 = new PermissionGrantRequest();

                permission2.setPermissionId(permissionId2);
                permission2.setGranted(true);

                BatchPermissionUpdateRequest request = new BatchPermissionUpdateRequest();

                request.setPermissions(
                                List.of(
                                                permission1,
                                                permission2));

                when(roleRepository.findById(roleId))
                                .thenReturn(java.util.Optional.of(role));

                BatchPermissionUpdateResponse response = rolePermissionBatchService.updatePermissions(
                                roleId,
                                request,
                                userId);

                /*
                 * Both permissions must be processed.
                 */
                verify(rolePermissionService)
                                .grantPermission(
                                                roleId,
                                                permissionId1,
                                                userId);

                verify(rolePermissionService)
                                .grantPermission(
                                                roleId,
                                                permissionId2,
                                                userId);

                assertEquals(
                                2,
                                response.getUpdatedCount());

                verify(roleRepository)
                                .saveAndFlush(role);
        }
}
