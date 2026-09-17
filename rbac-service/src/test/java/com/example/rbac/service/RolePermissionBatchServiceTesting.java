package com.example.rbac.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.rbac.dto.BatchPermissionUpdateRequest;
import com.example.rbac.dto.BatchPermissionUpdateResponse;
import com.example.rbac.dto.PermissionGrantRequest;
import com.example.rbac.entity.Role;
import com.example.rbac.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class RolePermissionBatchServiceTesting {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RolePermissionService rolePermissionService;

    @Mock
    private Role role;

    @Mock
    private PermissionGrantRequest grantRequest;

    @Mock
    private PermissionGrantRequest revokeRequest;

    @InjectMocks
    private RolePermissionBatchService batchService;

    private UUID roleId;
    private UUID permissionId1;
    private UUID permissionId2;
    private UUID changedBy;

    @BeforeEach
    void setUp() {

        roleId = UUID.randomUUID();
        permissionId1 = UUID.randomUUID();
        permissionId2 = UUID.randomUUID();
        changedBy = UUID.randomUUID();

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(role.getVersion())
                .thenReturn(1L);
    }

    @Test
    void updatePermissions_shouldProcessGrantAndRevoke() {

        when(grantRequest.getGranted())
                .thenReturn(true);

        when(grantRequest.getPermissionId())
                .thenReturn(permissionId1);

        when(revokeRequest.getGranted())
                .thenReturn(false);

        when(revokeRequest.getPermissionId())
                .thenReturn(permissionId2);

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);

        request.setPermissions(
                List.of(
                        grantRequest,
                        revokeRequest
                )
        );

        BatchPermissionUpdateResponse response =
                batchService.updatePermissions(
                        roleId,
                        request,
                        changedBy
                );

        assertNotNull(response);

        assertEquals(
                roleId,
                response.getRoleId()
        );

        assertEquals(
                2,
                response.getUpdatedCount()
        );

        assertEquals(
                "Permissions updated successfully",
                response.getMessage()
        );

        verify(rolePermissionService)
                .grantPermission(
                        roleId,
                        permissionId1,
                        changedBy
                );

        verify(rolePermissionService)
                .revokePermission(
                        roleId,
                        permissionId2,
                        changedBy
                );

        verify(role)
                .setUpdatedBy(changedBy);

        verify(roleRepository)
                .saveAndFlush(role);
    }

    @Test
    void updatePermissions_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.empty());

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);

        request.setPermissions(
                List.of(grantRequest)
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> batchService.updatePermissions(
                                roleId,
                                request,
                                changedBy
                        )
                );

        assertEquals(
                "Role not found",
                exception.getMessage()
        );

        verify(rolePermissionService, never())
                .grantPermission(
                        any(UUID.class),
                        any(UUID.class),
                        any(UUID.class)
                );

        verify(rolePermissionService, never())
                .revokePermission(
                        any(UUID.class),
                        any(UUID.class),
                        any(UUID.class)
                );

        verify(roleRepository, never())
                .saveAndFlush(any(Role.class));
    }

    @Test
    void updatePermissions_shouldRejectVersionMismatch() {

        when(role.getVersion())
                .thenReturn(2L);

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);

        request.setPermissions(
                List.of(grantRequest)
        );

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> batchService.updatePermissions(
                        roleId,
                        request,
                        changedBy
                )
        );

        verify(rolePermissionService, never())
                .grantPermission(
                        any(UUID.class),
                        any(UUID.class),
                        any(UUID.class)
                );

        verify(rolePermissionService, never())
                .revokePermission(
                        any(UUID.class),
                        any(UUID.class),
                        any(UUID.class)
                );

        verify(roleRepository, never())
                .saveAndFlush(any(Role.class));
    }

    @Test
    void updatePermissions_shouldHandleOnlyGrant() {

        when(grantRequest.getGranted())
                .thenReturn(true);

        when(grantRequest.getPermissionId())
                .thenReturn(permissionId1);

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);

        request.setPermissions(
                List.of(grantRequest)
        );

        BatchPermissionUpdateResponse response =
                batchService.updatePermissions(
                        roleId,
                        request,
                        changedBy
                );

        assertNotNull(response);

        assertEquals(
                1,
                response.getUpdatedCount()
        );

        verify(rolePermissionService)
                .grantPermission(
                        roleId,
                        permissionId1,
                        changedBy
                );

        verify(rolePermissionService, never())
                .revokePermission(
                        any(UUID.class),
                        any(UUID.class),
                        any(UUID.class)
                );

        verify(roleRepository)
                .saveAndFlush(role);
    }

    @Test
    void updatePermissions_shouldHandleOnlyRevoke() {

        when(revokeRequest.getGranted())
                .thenReturn(false);

        when(revokeRequest.getPermissionId())
                .thenReturn(permissionId2);

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);

        request.setPermissions(
                List.of(revokeRequest)
        );

        BatchPermissionUpdateResponse response =
                batchService.updatePermissions(
                        roleId,
                        request,
                        changedBy
                );

        assertNotNull(response);

        assertEquals(
                1,
                response.getUpdatedCount()
        );

        verify(rolePermissionService)
                .revokePermission(
                        roleId,
                        permissionId2,
                        changedBy
                );

        verify(rolePermissionService, never())
                .grantPermission(
                        any(UUID.class),
                        any(UUID.class),
                        any(UUID.class)
                );

        verify(roleRepository)
                .saveAndFlush(role);
    }
}