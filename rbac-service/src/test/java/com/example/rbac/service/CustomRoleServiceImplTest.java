package com.example.rbac.service;

import com.example.common.tenant.TenantContext;
import com.example.rbac.dto.CustomRoleRequest;
import com.example.rbac.dto.CustomRoleResponse;
import com.example.rbac.entity.CustomRoleConfig;
import com.example.rbac.entity.CustomRoleVersion;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.CustomRoleStatus;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.CustomRoleConfigRepository;
import com.example.rbac.repository.CustomRoleDataRepository;
import com.example.rbac.repository.CustomRoleVersionRepository;
import com.example.rbac.service.serviceImpl.CustomRoleServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomRoleServiceImplTest {

    @Mock
    private CustomRoleDataRepository roleRepository;

    @Mock
    private CustomRoleConfigRepository configRepository;

    @Mock
    private CustomRoleVersionRepository versionRepository;

    private ObjectMapper objectMapper;

    private CustomRoleServiceImpl customRoleService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final UUID TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID ROLE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        customRoleService = new CustomRoleServiceImpl(roleRepository, configRepository, versionRepository, objectMapper);

        tenantContextMock = mockStatic(TenantContext.class);

        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TENANT_ID.toString());
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void create_shouldCreateCustomRoleSuccessfully() {

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");
        request.setRoleCode("HR_MANAGER");
        request.setDescription("HR custom role");
        request.setPermissionIds(List.of(1L, 2L, 3L));
        request.setPublishNotes("Initial draft");

        when(roleRepository.existsByRoleNameIgnoreCaseAndTenantId("HR Manager", TENANT_ID.toString())).thenReturn(false);

        when(roleRepository.existsByRoleCodeIgnoreCaseAndTenantId("HR_MANAGER", TENANT_ID.toString())).thenReturn(false);

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {

            Role role = invocation.getArgument(0);

            role.setId(ROLE_ID);

            return role;
        });

        when(configRepository.save(any(CustomRoleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(versionRepository.save(any(CustomRoleVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomRoleResponse response = customRoleService.create(request);

        assertNotNull(response);

        assertEquals(ROLE_ID, response.getRoleId());

        assertEquals("HR Manager", response.getRoleName());

        assertEquals("HR_MANAGER", response.getRoleCode());

        assertEquals("HR custom role", response.getDescription());

        assertEquals(CustomRoleStatus.DRAFT, response.getStatus());

        assertEquals(1, response.getDraftVersion());

        assertEquals(0, response.getPublishedVersion());

        assertEquals(List.of(1L, 2L, 3L), response.getPermissionIds());

        assertEquals(3, response.getPermissionCount());

        verify(roleRepository).save(any(Role.class));

        verify(configRepository).save(any(CustomRoleConfig.class));

        verify(versionRepository).save(any(CustomRoleVersion.class));
    }

    @Test
    void create_shouldRejectDuplicateRoleName() {

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");

        when(roleRepository.existsByRoleNameIgnoreCaseAndTenantId("HR Manager", TENANT_ID.toString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> customRoleService.create(request));

        assertEquals("Role name already exists", exception.getMessage());

        verify(roleRepository, never()).save(any(Role.class));

        verify(configRepository, never()).save(any(CustomRoleConfig.class));
    }

    @Test
    void create_shouldRejectDuplicateRoleCode() {

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");
        request.setRoleCode("HR_MANAGER");

        when(roleRepository.existsByRoleNameIgnoreCaseAndTenantId("HR Manager", TENANT_ID.toString())).thenReturn(false);

        when(roleRepository.existsByRoleCodeIgnoreCaseAndTenantId("HR_MANAGER", TENANT_ID.toString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> customRoleService.create(request));

        assertEquals("Role code already exists", exception.getMessage());

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void create_shouldGenerateRoleCodeWhenCodeIsBlank() {

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");
        request.setRoleCode(null);
        request.setPermissionIds(List.of(1L));

        when(roleRepository.existsByRoleNameIgnoreCaseAndTenantId(anyString(), eq(TENANT_ID.toString()))).thenReturn(false);

        when(roleRepository.existsByRoleCodeIgnoreCaseAndTenantId(anyString(), eq(TENANT_ID.toString()))).thenReturn(false);

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {

            Role role = invocation.getArgument(0);

            role.setId(ROLE_ID);

            return role;
        });

        when(configRepository.save(any(CustomRoleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(versionRepository.save(any(CustomRoleVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomRoleResponse response = customRoleService.create(request);

        assertEquals("HR_MANAGER", response.getRoleCode());

        verify(roleRepository).existsByRoleCodeIgnoreCaseAndTenantId("HR_MANAGER", TENANT_ID.toString());
    }

    @Test
    void create_shouldHandleNullPermissions() {

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");
        request.setPermissionIds(null);

        when(roleRepository.existsByRoleNameIgnoreCaseAndTenantId(anyString(), eq(TENANT_ID.toString()))).thenReturn(false);

        when(roleRepository.existsByRoleCodeIgnoreCaseAndTenantId(anyString(), eq(TENANT_ID.toString()))).thenReturn(false);

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {

            Role role = invocation.getArgument(0);

            role.setId(ROLE_ID);

            return role;
        });

        when(configRepository.save(any(CustomRoleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(versionRepository.save(any(CustomRoleVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomRoleResponse response = customRoleService.create(request);

        assertNotNull(response);

        assertNotNull(response.getPermissionIds());

        assertTrue(response.getPermissionIds().isEmpty());

        assertEquals(0, response.getPermissionCount());
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void getAll_shouldReturnCustomRoles() {

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.DRAFT, 1, 0);

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleVersion version = createVersion(1);

        version.setPermissionSnapshot("[1,2,3]");

        when(configRepository.findAllByTenantId(TENANT_ID.toString())).thenReturn(List.of(config));

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(versionRepository.findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 1)).thenReturn(Optional.of(version));

        List<CustomRoleResponse> result = customRoleService.getAll();

        assertNotNull(result);

        assertEquals(1, result.size());

        assertEquals("HR Manager", result.get(0).getRoleName());

        assertEquals(List.of(1L, 2L, 3L), result.get(0).getPermissionIds());

        assertEquals(3, result.get(0).getPermissionCount());
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoRolesExist() {

        when(configRepository.findAllByTenantId(TENANT_ID.toString())).thenReturn(List.of());

        List<CustomRoleResponse> result = customRoleService.getAll();

        assertNotNull(result);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_shouldSkipDeletedRoles() {

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.DRAFT, 1, 0);

        Role role = createRole(ROLE_ID, "Deleted Role", "DELETED_ROLE");

        role.setIsDeleted(true);

        when(configRepository.findAllByTenantId(TENANT_ID.toString())).thenReturn(List.of(config));

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        List<CustomRoleResponse> result = customRoleService.getAll();

        assertTrue(result.isEmpty());

        verify(versionRepository, never()).findByRoleIdAndTenantIdAndVersionNumber(any(UUID.class), anyString(), anyInt());
    }

    @Test
    void getAll_shouldUsePublishedVersionWhenRoleIsPublished() {

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.PUBLISHED, 2, 1);

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleVersion version = createVersion(1);

        version.setPermissionSnapshot("[10,20]");

        when(configRepository.findAllByTenantId(TENANT_ID.toString())).thenReturn(List.of(config));

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(versionRepository.findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 1)).thenReturn(Optional.of(version));

        List<CustomRoleResponse> result = customRoleService.getAll();

        assertEquals(List.of(10L, 20L), result.get(0).getPermissionIds());

        verify(versionRepository).findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 1);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void update_shouldCreateNewDraftVersion() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.DRAFT, 1, 0);

        CustomRoleVersion existingVersion = createVersion(1);

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");
        request.setRoleCode("HR_MANAGER");
        request.setDescription("Updated HR role");
        request.setPermissionIds(List.of(1L, 2L));

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findTopByRoleIdAndTenantIdOrderByVersionNumberDesc(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(existingVersion));

        when(versionRepository.save(any(CustomRoleVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(configRepository.save(any(CustomRoleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomRoleResponse response = customRoleService.update(ROLE_ID, request);

        assertEquals(2, response.getDraftVersion());

        assertEquals(CustomRoleStatus.DRAFT, response.getStatus());

        assertEquals("Updated HR role", response.getDescription());

        assertEquals(List.of(1L, 2L), response.getPermissionIds());

        verify(versionRepository).save(any(CustomRoleVersion.class));

        verify(configRepository).save(config);

        verify(roleRepository).save(role);
    }

    @Test
    void update_shouldRejectArchivedRole() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.ARCHIVED, 1, 0);

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> customRoleService.update(ROLE_ID, request));

        assertEquals("Archived custom role cannot be updated", exception.getMessage());

        verify(versionRepository, never()).save(any());
    }

    @Test
    void update_shouldRejectDuplicateRoleName() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.DRAFT, 1, 0);

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("Finance Manager");

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(roleRepository.existsByRoleNameIgnoreCaseAndTenantId("Finance Manager", TENANT_ID.toString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> customRoleService.update(ROLE_ID, request));

        assertEquals("Role name already exists", exception.getMessage());

        verify(versionRepository, never()).save(any());
    }

    @Test
    void update_shouldRejectDuplicateRoleCode() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.DRAFT, 1, 0);

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");
        request.setRoleCode("FINANCE_MANAGER");

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(roleRepository.existsByRoleCodeIgnoreCaseAndTenantId("FINANCE_MANAGER", TENANT_ID.toString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> customRoleService.update(ROLE_ID, request));

        assertEquals("Role code already exists", exception.getMessage());

        verify(versionRepository, never()).save(any());
    }

    // =========================================================
    // PUBLISH
    // =========================================================

    @Test
    void publish_shouldPublishDraftVersion() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.DRAFT, 1, 0);

        CustomRoleVersion version = createVersion(1);

        version.setStatus(CustomRoleStatus.DRAFT);

        version.setPermissionSnapshot("[1,2,3]");

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 1)).thenReturn(Optional.of(version));

        when(versionRepository.save(any(CustomRoleVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(configRepository.save(any(CustomRoleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomRoleResponse response = customRoleService.publish(ROLE_ID, "Initial publication");

        assertEquals(CustomRoleStatus.PUBLISHED, response.getStatus());

        assertEquals(1, response.getPublishedVersion());

        assertEquals(CustomRoleStatus.PUBLISHED, version.getStatus());

        assertEquals("ACTIVE", role.getStatus());

        assertEquals(List.of(1L, 2L, 3L), response.getPermissionIds());

        verify(versionRepository).save(version);

        verify(configRepository).save(config);

        verify(roleRepository).save(role);
    }

    @Test
    void publish_shouldRejectArchivedRole() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.ARCHIVED, 1, 0);

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> customRoleService.publish(ROLE_ID, null));

        assertEquals("Archived custom role cannot be published", exception.getMessage());
    }

    @Test
    void publish_shouldRejectMissingDraftVersion() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.DRAFT, 1, 0);

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 1)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> customRoleService.publish(ROLE_ID, null));

        assertEquals("Draft version not found", exception.getMessage());
    }

    @Test
    void publish_shouldRejectAlreadyPublishedVersion() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.PUBLISHED, 1, 1);

        CustomRoleVersion version = createVersion(1);

        version.setStatus(CustomRoleStatus.PUBLISHED);

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 1)).thenReturn(Optional.of(version));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> customRoleService.publish(ROLE_ID, null));

        assertEquals("Only a draft version can be published", exception.getMessage());

        verify(versionRepository, never()).save(any());
    }

    // =========================================================
    // ARCHIVE
    // =========================================================

    @Test
    void archive_shouldArchiveRole() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.PUBLISHED, 1, 1);

        CustomRoleVersion version = createVersion(1);

        version.setPermissionSnapshot("[1,2]");

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 1)).thenReturn(Optional.of(version));

        when(configRepository.save(any(CustomRoleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomRoleResponse response = customRoleService.archive(ROLE_ID);

        assertEquals(CustomRoleStatus.ARCHIVED, response.getStatus());

        assertEquals("ARCHIVED", role.getStatus());

        assertTrue(role.getIsDeleted());

        assertEquals(CustomRoleStatus.ARCHIVED, config.getStatus());

        verify(configRepository).save(config);

        verify(roleRepository).save(role);
    }

    // =========================================================
    // GET VERSIONS
    // =========================================================

    @Test
    void getVersions_shouldReturnVersionsDescending() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.PUBLISHED, 2, 1);

        CustomRoleVersion version2 = createVersion(2);

        version2.setPermissionSnapshot("[1,2,3]");

        CustomRoleVersion version1 = createVersion(1);

        version1.setStatus(CustomRoleStatus.PUBLISHED);

        version1.setPermissionSnapshot("[1,2]");

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findAllByRoleIdAndTenantIdOrderByVersionNumberDesc(ROLE_ID, TENANT_ID.toString())).thenReturn(List.of(version2, version1));

        List<CustomRoleResponse> result = customRoleService.getVersions(ROLE_ID);

        assertEquals(2, result.size());

        assertEquals(2, result.get(0).getVersionNumber());

        assertEquals(1, result.get(1).getVersionNumber());

        assertEquals(List.of(1L, 2L, 3L), result.get(0).getPermissionIds());

        assertEquals(List.of(1L, 2L), result.get(1).getPermissionIds());
    }

    @Test
    void getVersions_shouldReturnEmptyListWhenNoVersions() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.DRAFT, 1, 0);

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findAllByRoleIdAndTenantIdOrderByVersionNumberDesc(ROLE_ID, TENANT_ID.toString())).thenReturn(List.of());

        List<CustomRoleResponse> result = customRoleService.getVersions(ROLE_ID);

        assertNotNull(result);

        assertTrue(result.isEmpty());
    }

    // =========================================================
    // REVERT
    // =========================================================

    @Test
    void revert_shouldCreateNewDraftFromHistoricalVersion() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.PUBLISHED, 2, 1);

        CustomRoleVersion oldVersion = createVersion(1);

        oldVersion.setStatus(CustomRoleStatus.PUBLISHED);

        oldVersion.setPermissionSnapshot("[1,2]");

        CustomRoleVersion latestVersion = createVersion(2);

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 1)).thenReturn(Optional.of(oldVersion));

        when(versionRepository.findTopByRoleIdAndTenantIdOrderByVersionNumberDesc(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(latestVersion));

        when(versionRepository.save(any(CustomRoleVersion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(configRepository.save(any(CustomRoleConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomRoleResponse response = customRoleService.revert(ROLE_ID, 1);

        assertEquals(CustomRoleStatus.DRAFT, response.getStatus());

        assertEquals(3, response.getDraftVersion());

        assertEquals(List.of(1L, 2L), response.getPermissionIds());

        ArgumentCaptor<CustomRoleVersion> captor = ArgumentCaptor.forClass(CustomRoleVersion.class);

        verify(versionRepository).save(captor.capture());

        CustomRoleVersion savedVersion = captor.getValue();

        assertEquals(ROLE_ID, savedVersion.getRoleId());

        assertEquals(3, savedVersion.getVersionNumber());

        assertEquals(CustomRoleStatus.DRAFT, savedVersion.getStatus());

        assertEquals("[1,2]", savedVersion.getPermissionSnapshot());
    }

    @Test
    void revert_shouldRejectUnknownVersion() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        CustomRoleConfig config = createConfig(ROLE_ID, CustomRoleStatus.PUBLISHED, 1, 1);

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        when(configRepository.findByRoleIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(config));

        when(versionRepository.findByRoleIdAndTenantIdAndVersionNumber(ROLE_ID, TENANT_ID.toString(), 99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> customRoleService.revert(ROLE_ID, 99));

        assertEquals("Version not found", exception.getMessage());

        verify(versionRepository, never()).save(any());
    }

    // =========================================================
    // IMPACT
    // =========================================================

    @Test
    void getImpact_shouldReturnPendingIntegrationResponse() {

        Role role = createRole(ROLE_ID, "HR Manager", "HR_MANAGER");

        when(roleRepository.findByIdAndTenantId(ROLE_ID, TENANT_ID.toString())).thenReturn(Optional.of(role));

        Object result = customRoleService.getImpact(ROLE_ID);

        assertNotNull(result);

        assertTrue(result instanceof Map);

        Map<?, ?> impact = (Map<?, ?>) result;

        assertEquals(ROLE_ID, impact.get("roleId"));

        assertEquals("Impact analysis integration is pending", impact.get("message"));
    }

    // =========================================================
    // LIMITS
    // =========================================================

    @Test
    void getLimits_shouldReturnCurrentRoleCount() {

        when(configRepository.countByTenantIdAndStatusNot(TENANT_ID.toString(), CustomRoleStatus.ARCHIVED)).thenReturn(5L);

        Object result = customRoleService.getLimits();

        assertNotNull(result);

        assertTrue(result instanceof Map);

        Map<?, ?> limits = (Map<?, ?>) result;

        assertEquals(TENANT_ID.toString(), limits.get("tenantId"));

        assertEquals(5L, limits.get("currentCustomRoles"));

        assertEquals("LICENSE_INTEGRATION_PENDING", limits.get("limit"));
    }

    // =========================================================
    // TENANT CONTEXT
    // =========================================================

    @Test
    void create_shouldRejectWhenTenantContextIsMissing() {

        tenantContextMock.when(TenantContext::getTenantId).thenReturn(null);

        CustomRoleRequest request = new CustomRoleRequest();

        request.setRoleName("HR Manager");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> customRoleService.create(request));

        assertEquals("Tenant context is not available", exception.getMessage());

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(configRepository);
        verifyNoInteractions(versionRepository);
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private Role createRole(UUID id, String roleName, String roleCode) {

        Role role = new Role();

        role.setId(id);

        role.setRoleName(roleName);

        role.setRoleCode(roleCode);

        role.setRoleType(RoleType.CUSTOM);

        role.setDescription("Test role");

        role.setStatus("DRAFT");

        role.setIsDeleted(false);

        role.setTenantId(TENANT_ID);

        return role;
    }

    private CustomRoleConfig createConfig(UUID roleId, CustomRoleStatus status, int draftVersion, int publishedVersion) {

        CustomRoleConfig config = new CustomRoleConfig();

        config.setRoleId(roleId);

        /*
         * If your CustomRoleConfig currently has
         * String tenantId, use TENANT_ID.toString().
         *
         * If it has UUID tenantId, use TENANT_ID.
         */
        config.setTenantId(TENANT_ID.toString());

        config.setDraftVersion(draftVersion);

        config.setPublishedVersion(publishedVersion);

        config.setStatus(status);

        return config;
    }

    private CustomRoleVersion createVersion(int versionNumber) {

        CustomRoleVersion version = new CustomRoleVersion();

        version.setRoleId(ROLE_ID);

        /*
         * If your CustomRoleVersion currently has
         * String tenantId, use TENANT_ID.toString().
         *
         * If it has UUID tenantId, use TENANT_ID.
         */
        version.setTenantId(TENANT_ID.toString());

        version.setVersionNumber(versionNumber);

        version.setStatus(CustomRoleStatus.DRAFT);

        version.setPermissionSnapshot("[]");

        return version;
    }
}