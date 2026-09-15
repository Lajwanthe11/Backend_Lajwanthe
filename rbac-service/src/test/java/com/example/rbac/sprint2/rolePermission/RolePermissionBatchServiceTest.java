package com.example.qa.sprint2.rolePermission;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

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
import com.example.rbac.service.RolePermissionBatchService;
import com.example.rbac.service.RolePermissionService;

@ExtendWith(MockitoExtension.class)
class RolePermissionBatchServiceTest {

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

    private final Long roleId = 1L;
    private final UUID permissionId1 = UUID.randomUUID();
    private final UUID permissionId2 = UUID.randomUUID();

    @BeforeEach
    void setUp() {

        when(roleRepository.findById(roleId))
                .thenReturn(Optional.of(role));

        when(role.getVersion()).thenReturn(1L);
    }

    @Test
    void updatePermissions_shouldProcessGrantAndRevoke() {

        when(grantRequest.getGranted()).thenReturn(true);
        when(grantRequest.getPermissionId())
                .thenReturn(permissionId1);

        when(revokeRequest.getGranted()).thenReturn(false);
        when(revokeRequest.getPermissionId())
                .thenReturn(permissionId2);

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);

        request.setPermissions(
                List.of(grantRequest, revokeRequest));

        BatchPermissionUpdateResponse response =
                batchService.updatePermissions(
                        roleId,
                        request,
                        "SYSTEM");

        assertNotNull(response);
        assertEquals(roleId, response.getRoleId());
        assertEquals(2, response.getUpdatedCount());
        assertEquals(
                "Permissions updated successfully",
                response.getMessage());

        verify(rolePermissionService)
                .grantPermission(
                        roleId,
                        permissionId1,
                        "SYSTEM");

        verify(rolePermissionService)
                .revokePermission(
                        roleId,
                        permissionId2,
                        "SYSTEM");

        verify(role).setUpdatedBy("SYSTEM");

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
        request.setPermissions(List.of(grantRequest));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> batchService.updatePermissions(
                                roleId,
                                request,
                                "SYSTEM"));

        assertEquals(
                "Role not found",
                exception.getMessage());

        verify(rolePermissionService, never())
                .grantPermission(anyLong(), any(), anyString());
    }

    @Test
    void updatePermissions_shouldRejectVersionMismatch() {

        when(role.getVersion()).thenReturn(2L);

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);
        request.setPermissions(List.of(grantRequest));

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> batchService.updatePermissions(
                        roleId,
                        request,
                        "SYSTEM"));

        verify(rolePermissionService, never())
                .grantPermission(anyLong(), any(), anyString());

        verify(rolePermissionService, never())
                .revokePermission(anyLong(), any(), anyString());

        verify(roleRepository, never())
                .saveAndFlush(any());
    }

    @Test
    void updatePermissions_shouldHandleOnlyGrant() {

        when(grantRequest.getGranted()).thenReturn(true);
        when(grantRequest.getPermissionId())
                .thenReturn(permissionId1);

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);
        request.setPermissions(List.of(grantRequest));

        BatchPermissionUpdateResponse response =
                batchService.updatePermissions(
                        roleId,
                        request,
                        "SYSTEM");

        assertEquals(1, response.getUpdatedCount());

        verify(rolePermissionService)
                .grantPermission(
                        roleId,
                        permissionId1,
                        "SYSTEM");

        verify(rolePermissionService, never())
                .revokePermission(
                        anyLong(),
                        any(),
                        anyString());
    }

    @Test
    void updatePermissions_shouldHandleOnlyRevoke() {

        when(revokeRequest.getGranted()).thenReturn(false);
        when(revokeRequest.getPermissionId())
                .thenReturn(permissionId2);

        BatchPermissionUpdateRequest request =
                new BatchPermissionUpdateRequest();

        request.setExpectedVersion(1L);
        request.setPermissions(List.of(revokeRequest));

        BatchPermissionUpdateResponse response =
                batchService.updatePermissions(
                        roleId,
                        request,
                        "SYSTEM");

        assertEquals(1, response.getUpdatedCount());

        verify(rolePermissionService)
                .revokePermission(
                        roleId,
                        permissionId2,
                        "SYSTEM");

        verify(rolePermissionService, never())
                .grantPermission(
                        anyLong(),
                        any(),
                        anyString());
    }
}