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
import com.example.rbac.exception.RoleNotFoundException;
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

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceFeatureTest {

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
                .thenReturn(tenantId.toString());

        roleService = new RoleServiceImpl(
                roleRepository,
                roleTemplateRepository,
                currentUserContext,
                roleHistoryRepository,
                roleExportService,
                permissionRepository
        );
    }

    // =================================================================
    // Added scenarios: create() duplicate-name and template flow
    // =================================================================

    @Test
    void createRole_throwsWhenNameAlreadyExists() {

        RoleRequestDto request = new RoleRequestDto();
        request.setRoleName("Duplicate Role");

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Duplicate Role", tenantId))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> roleService.create(request));

        verify(roleRepository, never()).save(any());
    }

    @Test
    void createRole_fromTemplate_whenNoExplicitPermissionsGiven() {

        UUID templateId = UUID.randomUUID();
        RoleTemplate template = new RoleTemplate();
        template.setId(templateId);
        template.setName("HR Starter");
        Permission empView = new Permission();
        empView.setPermissionCode("EMPLOYEE_VIEW");
        template.setPermissions(new HashSet<>(Set.of(empView)));

        RoleRequestDto request = new RoleRequestDto();
        request.setRoleName("New HR Role");
        request.setTemplateId(templateId);
        // permissionCodes intentionally left unset — should fall back to
        // the template's own permissions.

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        anyString(), eq(tenantId)))
                .thenReturn(false);
        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        anyString(), eq(tenantId)))
                .thenReturn(false);
        when(roleTemplateRepository.findById(String.valueOf(templateId)))
                .thenReturn(java.util.Optional.of(template));
        when(permissionRepository.findByPermissionCodeIn(any()))
                .thenReturn(List.of(empView));
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        roleService.create(request);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertEquals(templateId.toString(), captor.getValue().getCreatedFromTemplateId());
    }

    // =================================================================
    // Added scenarios: update() duplicate-name rejection
    // =================================================================

    @Test
    void updateRole_throwsWhenRenamedToExistingRoleName() {

        Role existingRole = new Role();
        existingRole.setId(roleId);
        existingRole.setTenantId(tenantId);
        existingRole.setRoleName("HR Manager");
        existingRole.setRoleCode("HR_MANAGER");
        existingRole.setRoleType(RoleType.CUSTOM);
        existingRole.setStatus("ACTIVE");
        existingRole.setIsDeleted(false);

        RoleRequestDto request = new RoleRequestDto();
        request.setRoleName("Finance Manager");

        when(roleRepository.findByIdAndTenantIdAndIsDeletedFalse(any(), any()))
                .thenReturn(java.util.Optional.of(existingRole));
        when(roleRepository.existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                "Finance Manager", tenantId))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> roleService.update(roleId, request));

        verify(roleRepository, never()).save(any());
    }

    // =================================================================
    // Added scenarios: template visibility
    // =================================================================

    @Test
    void updateTemplateVisibility_setsHiddenFlag() {

        UUID templateId = UUID.randomUUID();
        RoleTemplate template = new RoleTemplate();
        template.setId(templateId);
        template.setHidden(false);

        when(roleTemplateRepository.findById(String.valueOf(templateId)))
                .thenReturn(java.util.Optional.of(template));
        when(roleTemplateRepository.save(any(RoleTemplate.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        roleService.updateTemplateVisibility(templateId, true);

        assertTrue(template.isHidden());
        verify(roleTemplateRepository).save(template);
    }

    @Test
    void listTemplates_superAdmin_seesHiddenTemplates() {

        when(currentUserContext.hasRole("SUPER_ADMIN")).thenReturn(true);
        when(roleTemplateRepository.findAll()).thenReturn(List.of());

        roleService.listTemplates();

        verify(roleTemplateRepository).findAll();
        verify(roleTemplateRepository, never()).findAllByHiddenFalse();
    }

    @Test
    void listTemplates_regularUser_seesOnlyVisibleTemplates() {

        when(currentUserContext.hasRole("SUPER_ADMIN")).thenReturn(false);
        when(roleTemplateRepository.findAllByHiddenFalse()).thenReturn(List.of());

        roleService.listTemplates();

        verify(roleTemplateRepository).findAllByHiddenFalse();
        verify(roleTemplateRepository, never()).findAll();
    }

    @Test
    void compareRoles_splitsSharedAndUniquePermissions() {

        UUID role1Id = UUID.randomUUID();
        UUID role2Id = UUID.randomUUID();

        Permission read = new Permission();
        read.setPermissionCode("USER_READ");
        Permission write = new Permission();
        write.setPermissionCode("USER_WRITE");
        Permission delete = new Permission();
        delete.setPermissionCode("USER_DELETE");

        Role role1 = new Role();
        role1.setId(role1Id);
        role1.setTenantId(tenantId);
        role1.setRoleName("Manager");
        role1.setPermissions(new HashSet<>(Set.of(read, write)));

        Role role2 = new Role();
        role2.setId(role2Id);
        role2.setTenantId(tenantId);
        role2.setRoleName("Viewer");
        role2.setPermissions(new HashSet<>(Set.of(read, delete)));

        when(roleRepository.findByIdAndTenantId(role1Id.toString(), tenantId))
                .thenReturn(java.util.Optional.of(role1));
        when(roleRepository.findByIdAndTenantId(role2Id.toString(), tenantId))
                .thenReturn(java.util.Optional.of(role2));

        RoleCompareResponse response =
                roleService.compareRoles(role1Id.toString(), role2Id.toString());

        assertTrue(response.sharedPermissions().contains("USER_READ"));
        assertTrue(response.onlyInRole1().contains("USER_WRITE"));
        assertTrue(response.onlyInRole2().contains("USER_DELETE"));
    }

    @Test
    void compareRoles_throwsWhenEitherRoleMissing() {

        UUID role1Id = UUID.randomUUID();
        UUID role2Id = UUID.randomUUID();

        when(roleRepository.findByIdAndTenantId(role1Id.toString(), tenantId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                RoleNotFoundException.class,
                () -> roleService.compareRoles(role1Id.toString(), role2Id.toString()));
    }

    @Test
    void cloneRole_producesCustomRoleWithUniqueCode() {

        UUID sourceId = UUID.randomUUID();
        Role source = new Role();
        source.setId(sourceId);
        source.setTenantId(tenantId);
        source.setRoleName("Admin");
        source.setRoleCode("ADMIN");
        source.setRoleType(RoleType.SYSTEM);
        Permission userRead = new Permission();
        userRead.setPermissionCode("USER_READ");
        source.setPermissions(new HashSet<>(Set.of(userRead)));

        when(roleRepository.findByIdAndTenantId(sourceId.toString(), tenantId))
                .thenReturn(java.util.Optional.of(source));
        when(roleRepository.existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                anyString(), eq(tenantId)))
                .thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenAnswer(inv -> {
                    Role r = inv.getArgument(0);
                    r.setId(UUID.randomUUID());
                    return r;
                });

        RoleCloneRequest request = new RoleCloneRequest("Admin Copy");
        RoleResponseDto result = roleService.cloneRole(sourceId.toString(), request);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        Role saved = captor.getValue();

        assertEquals(RoleType.CUSTOM, saved.getRoleType());
        assertEquals(sourceId, saved.getClonedFromRoleId());
        assertEquals(1, saved.getPermissions().size());
        verify(roleHistoryRepository).save(any(RoleHistory.class));
        assertNotNull(result);
    }

    @Test
    void cloneRole_throwsWhenSourceRoleNotInTenant() {

        UUID sourceId = UUID.randomUUID();
        when(roleRepository.findByIdAndTenantId(sourceId.toString(), tenantId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                RoleNotFoundException.class,
                () -> roleService.cloneRole(sourceId.toString(), new RoleCloneRequest("X")));
    }

    @Test
    void getHistory_returnsEmptyList_whenNoChangesRecorded() {

        Role role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);

        when(roleRepository.findByIdAndTenantId(roleId.toString(), tenantId))
                .thenReturn(java.util.Optional.of(role));
        when(roleHistoryRepository.findAllByRoleIdOrderByChangedAtDesc(roleId.toString()))
                .thenReturn(List.of());

        var history = roleService.getHistory(roleId.toString());

        assertTrue(history.isEmpty());
    }

    @Test
    void getHistory_throwsWhenRoleNotInTenant() {

        when(roleRepository.findByIdAndTenantId(roleId.toString(), tenantId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                RoleNotFoundException.class,
                () -> roleService.getHistory(roleId.toString()));
    }

    // =================================================================
    // Added scenario: exportRoles() tenant scoping
    // =================================================================

    @Test
    void exportRoles_usesTenantFromContext() {

        Role role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);
        role.setRoleName("HR Manager");
        role.setPermissions(new HashSet<>());

        List<Role> roles = List.of(role);

        when(roleRepository.findAllByTenantId(tenantId)).thenReturn(roles);
        when(roleExportService.export(roles, "PDF")).thenReturn(new byte[]{1, 2, 3});

        byte[] result = roleService.exportRoles("PDF");

        assertEquals(3, result.length);
        verify(roleRepository).findAllByTenantId(tenantId);
    }

}