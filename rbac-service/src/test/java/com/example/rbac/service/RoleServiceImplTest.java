package com.example.rbac.service;

import com.example.common.exception.BadRequestException;
import com.example.rbac.dto.RoleCloneRequest;
import com.example.rbac.dto.RoleCompareResponse;
import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RoleHistory;
import com.example.rbac.entity.RoleTemplate;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RoleHistoryRepository;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.RoleTemplateRepository;
import com.example.rbac.service.serviceImpl.RoleServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleTemplateRepository roleTemplateRepository;

    @Mock
    private CurrentUserContext currentUserContext;

    @Mock
    private RoleHistoryRepository roleHistoryRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleExportService roleExportService;

    private RoleServiceImpl roleService;

    private UUID roleId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {

        roleId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        lenient().when(currentUserContext.getTenantId())
                .thenReturn(tenantId);

        roleService = new RoleServiceImpl(
                roleRepository,
                roleTemplateRepository,
                currentUserContext,
                roleHistoryRepository,
                roleExportService,
                permissionRepository
        );
    }

    // Test successful role creation
    @Test
    void createRole_success() {

        RoleRequestDto request = new RoleRequestDto();
        request.setRoleName("HR Manager");
        request.setRoleType(RoleType.CUSTOM);
        request.setDescription("HR role");
        request.setStatus("ACTIVE");

        // Duplicate role name does not exist
        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        anyString(), any()))
                .thenReturn(false);

        // Duplicate role code does not exist
        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        anyString(), any()))
                .thenReturn(false);

        Role savedRole = new Role();
        savedRole.setId(roleId);
        savedRole.setTenantId(tenantId);
        savedRole.setRoleName("HR Manager");
        savedRole.setRoleCode("HR_MANAGER");
        savedRole.setRoleType(RoleType.CUSTOM);
        savedRole.setDescription("HR role");
        savedRole.setStatus("ACTIVE");
        savedRole.setIsDeleted(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto response = roleService.create(request);

        assertNotNull(response);
        assertEquals(roleId, response.getId());
        assertEquals("HR Manager", response.getRoleName());
        assertEquals("HR_MANAGER", response.getRoleCode());
        assertEquals(RoleType.CUSTOM, response.getRoleType());
        assertEquals("ACTIVE", response.getStatus());
    }

    // Test successful role status update
    @Test
    void updateStatus_success() {

        Role role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);
        role.setRoleName("HR Manager");
        role.setRoleCode("HR_MANAGER");
        role.setRoleType(RoleType.CUSTOM);
        role.setStatus("ACTIVE");
        role.setIsDeleted(false);

        when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                any(), any()))
                .thenReturn(java.util.Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        RoleResponseDto response =
                roleService.updateStatus(roleId, "INACTIVE");

        assertEquals("INACTIVE", response.getStatus());
    }

    // Test soft delete of a role
    @Test
    void deleteRole_softDelete_success() {

        Role role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);
        role.setRoleName("HR Manager");
        role.setRoleCode("HR_MANAGER");
        role.setRoleType(RoleType.CUSTOM);
        role.setStatus("ACTIVE");
        role.setIsDeleted(false);

        when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                any(), any()))
                .thenReturn(java.util.Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        roleService.deleteById(roleId);

        assertEquals(true, role.getIsDeleted());
    }

    // Test fetching role by ID
    @Test
    void getRoleById_success() {

        Role role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);
        role.setRoleName("HR Manager");
        role.setRoleCode("HR_MANAGER");
        role.setRoleType(RoleType.CUSTOM);
        role.setStatus("ACTIVE");
        role.setIsDeleted(false);

        when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                any(), any()))
                .thenReturn(java.util.Optional.of(role));

        RoleResponseDto response = roleService.getById(roleId);

        assertNotNull(response);
        assertEquals(roleId, response.getId());
        assertEquals("HR Manager", response.getRoleName());
    }

    // Test role search and filter
    @Test
    void searchRoles_success() {

        Role role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);
        role.setRoleName("HR Manager");
        role.setRoleCode("HR_MANAGER");
        role.setRoleType(RoleType.CUSTOM);
        role.setStatus("ACTIVE");
        role.setIsDeleted(false);

        when(roleRepository.searchRoles(
                any(), any(), any(), any()))
                .thenReturn(java.util.List.of(role));

        java.util.List<RoleResponseDto> result =
                roleService.searchRoles(
                        "HR",
                        RoleType.CUSTOM,
                        "ACTIVE"
                );

        assertEquals(1, result.size());
        assertEquals("HR Manager", result.get(0).getRoleName());
    }

    // Test role dashboard counts
    @Test
    void getRoleCounts_success() {

        when(roleRepository.countByTenantIdAndIsDeletedFalse(any()))
                .thenReturn(5L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        any(), any()))
                .thenReturn(2L, 3L);

        java.util.Map<String, Long> counts =
                roleService.getRoleCounts();

        assertEquals(5L, counts.get("totalRoles"));
        assertEquals(2L, counts.get("systemRoles"));
        assertEquals(3L, counts.get("customRoles"));
    }

    // Test successful role update
    @Test
    void updateRole_success() {

        Role existingRole = new Role();
        existingRole.setId(roleId);
        existingRole.setTenantId(tenantId);
        existingRole.setRoleName("HR Manager");
        existingRole.setRoleCode("HR_MANAGER");
        existingRole.setRoleType(RoleType.CUSTOM);
        existingRole.setDescription("Old description");
        existingRole.setStatus("ACTIVE");
        existingRole.setIsDeleted(false);

        RoleRequestDto request = new RoleRequestDto();
        request.setRoleName("HR Lead");
        request.setDescription("Updated description");

        when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                any(), any()))
                .thenReturn(java.util.Optional.of(existingRole));

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        anyString(), any()))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(existingRole);

        RoleResponseDto response =
                roleService.update(roleId, request);

        assertEquals("HR Lead", response.getRoleName());
        assertEquals("Updated description", response.getDescription());

        // Code and type should not change during update
        assertEquals("HR_MANAGER", response.getRoleCode());
        assertEquals(RoleType.CUSTOM, response.getRoleType());
    }

    // Test invalid role status
    @Test
    void updateStatus_invalidStatus() {

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> roleService.updateStatus(roleId, "PENDING")
        );
    }

    // Test role not found scenario
    @Test
    void getRoleById_notFound() {

        when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                any(), any()))
                .thenReturn(java.util.Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> roleService.getById(roleId)
        );
    }

    // Test protected system role deletion
    @Test
    void deleteProtectedSystemRole_shouldThrowException() {

        Role role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);
        role.setRoleName("Admin");
        role.setRoleCode("ADMIN");
        role.setRoleType(RoleType.SYSTEM);
        role.setStatus("ACTIVE");
        role.setIsDeleted(false);

        when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(
                any(), any()))
                .thenReturn(java.util.Optional.of(role));

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> roleService.deleteById(roleId));
    }
}