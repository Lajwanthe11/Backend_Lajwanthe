package com.example.rbac.service;

import com.example.common.exception.BadRequestException;
import com.example.rbac.entity.RoleDepartmentMap;
import com.example.rbac.repository.RoleDepartmentMapRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.UserRoleRepository;
import com.example.rbac.service.serviceImpl.DepartmentPermissionServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentPermissionServiceImplTest {

    @Mock
    private RoleDepartmentMapRepository repository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    private DepartmentPermissionServiceImpl departmentPermissionService;

    private UUID userRoleId;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        userRoleId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        departmentPermissionService = new DepartmentPermissionServiceImpl(
                repository,
                userRoleRepository,
                rolePermissionRepository
        );
    }

    // Test fetching department scope for a role assignment
    @Test
    void getDepartmentScope_success() {

        RoleDepartmentMap map = new RoleDepartmentMap();
        map.setUserRoleId(userRoleId);
        map.setDepartmentId(departmentId);

        when(repository.findByUserRoleId(userRoleId))
                .thenReturn(List.of(map));

        List<UUID> result = departmentPermissionService.getDepartmentScope(userRoleId);

        assertEquals(1, result.size());
        assertEquals(departmentId, result.get(0));
    }

    // Test fetching scope when none exists
    @Test
    void getDepartmentScope_empty() {

        when(repository.findByUserRoleId(userRoleId))
                .thenReturn(List.of());

        List<UUID> result = departmentPermissionService.getDepartmentScope(userRoleId);

        assertTrue(result.isEmpty());
    }

    // Test successful update of department scope
    @Test
    void updateDepartmentScope_success() {

        List<UUID> departmentIds = List.of(departmentId, UUID.randomUUID());

        departmentPermissionService.updateDepartmentScope(userRoleId, departmentIds);

        verify(repository, times(1)).deleteByUserRoleId(userRoleId);
        verify(repository, times(departmentIds.size())).save(any(RoleDepartmentMap.class));
    }

    // Test update with empty list throws exception
    @Test
    void updateDepartmentScope_emptyList_throwsException() {

        assertThrows(
                BadRequestException.class,
                () -> departmentPermissionService.updateDepartmentScope(userRoleId, List.of())
        );
    }

    // Test update with null list throws exception
    @Test
    void updateDepartmentScope_nullList_throwsException() {

        assertThrows(
                BadRequestException.class,
                () -> departmentPermissionService.updateDepartmentScope(userRoleId, null)
        );
    }

    // Test update with duplicate department IDs throws exception
    @Test
    void updateDepartmentScope_duplicateIds_throwsException() {

        List<UUID> departmentIds = List.of(departmentId, departmentId);

        assertThrows(
                BadRequestException.class,
                () -> departmentPermissionService.updateDepartmentScope(userRoleId, departmentIds)
        );
    }

    // Test successful removal of all department scope
    @Test
    void removeAllDepartmentScope_success() {

        departmentPermissionService.removeAllDepartmentScope(userRoleId);

        verify(repository, times(1)).deleteByUserRoleId(userRoleId);
    }

    // Test department scope report returns correct data
    @Test
    void getDeptScopeReport_success() {

        RoleDepartmentMap map = new RoleDepartmentMap();
        map.setUserRoleId(userRoleId);
        map.setDepartmentId(departmentId);
        map.setCreatedBy("system");

        when(repository.findAll())
                .thenReturn(List.of(map));

        List<com.example.rbac.dto.DeptScopeReportDto> result =
                departmentPermissionService.getDeptScopeReport();

        assertEquals(1, result.size());
        assertEquals(userRoleId, result.get(0).getUserRoleId());
        assertEquals(departmentId, result.get(0).getDepartmentId());
        assertEquals("system", result.get(0).getCreatedBy());
    }
}