package com.example.qa.sprint2.role;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.common.exception.BadRequestException;
import com.example.common.exception.ResourceNotFoundException;
import com.example.common.tenant.TenantContext;

import com.example.rbac.dto.RoleRequestDto;
import com.example.rbac.dto.RoleResponseDto;
import com.example.rbac.dto.RoleTemplateDetailDto;
import com.example.rbac.dto.RoleTemplateSummaryDto;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RoleTemplate;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.RoleTemplateRepository;
import com.example.rbac.service.CurrentUserContext;
import com.example.rbac.service.serviceImpl.RoleServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleTemplateRepository roleTemplateRepository;

    @Mock
    private CurrentUserContext currentUser;

    @InjectMocks
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("tenant-a");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Test
    void createRole_shouldCreateRoleForCurrentTenant() {

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "HR manager",
                "ACTIVE"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager", "tenant-a"))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER", "tenant-a"))
                .thenReturn(false);

        Role savedRole = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result = roleService.create(request);

        assertNotNull(result);
        assertEquals("HR Manager", result.getRoleName());
        assertEquals("HR_MANAGER", result.getRoleCode());
        assertEquals(RoleType.CUSTOM, result.getRoleType());

        ArgumentCaptor<Role> captor =
                ArgumentCaptor.forClass(Role.class);

        verify(roleRepository).save(captor.capture());

        Role role = captor.getValue();

        assertEquals("tenant-a", role.getTenantId());
        assertFalse(role.getIsDeleted());
        assertEquals("ACTIVE", role.getStatus());
    }

    @Test
    void createRole_shouldGenerateRoleCodeWhenCodeIsMissing() {

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                null,
                RoleType.CUSTOM,
                "HR role",
                null
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager", "tenant-a"))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER", "tenant-a"))
                .thenReturn(false);

        Role savedRole = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result = roleService.create(request);

        assertEquals("HR_MANAGER", result.getRoleCode());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    void createRole_shouldRejectDuplicateRoleName() {

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "HR role",
                "ACTIVE"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager", "tenant-a"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> roleService.create(request)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void createRole_shouldRejectDuplicateRoleCode() {

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "HR role",
                "ACTIVE"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager", "tenant-a"))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER", "tenant-a"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> roleService.create(request)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Test
    void getById_shouldReturnRoleForCurrentTenant() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L, "tenant-a"))
                .thenReturn(Optional.of(role));

        RoleResponseDto result = roleService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("HR Manager", result.getRoleName());
        assertEquals("HR_MANAGER", result.getRoleCode());

        verify(roleRepository)
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L, "tenant-a");
    }

    @Test
    void getById_shouldThrowExceptionWhenRoleDoesNotExist() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L, "tenant-a"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getById(1L)
        );
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void getAll_shouldReturnOnlyCurrentTenantRoles() {

        Role role1 = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        Role role2 = createRole(
                2L,
                "Finance Manager",
                "FINANCE_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse("tenant-a"))
                .thenReturn(List.of(role1, role2));

        List<RoleResponseDto> result =
                roleService.getAll();

        assertEquals(2, result.size());

        assertEquals(
                "HR Manager",
                result.get(0).getRoleName()
        );

        assertEquals(
                "Finance Manager",
                result.get(1).getRoleName()
        );

        verify(roleRepository)
                .findByTenantIdAndIsDeletedFalse("tenant-a");
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoRolesExist() {

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse("tenant-a"))
                .thenReturn(List.of());

        List<RoleResponseDto> result =
                roleService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // PAGINATION
    // =========================================================

    @Test
    void getAllPaged_shouldUseTenantAwareQuery() {

        PageRequest pageable =
                PageRequest.of(0, 10);

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        Page<Role> page =
                new PageImpl<>(
                        List.of(role),
                        pageable,
                        1
                );

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(
                        "tenant-a",
                        pageable))
                .thenReturn(page);

        Page<RoleResponseDto> result =
                roleService.getAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "HR Manager",
                result.getContent().get(0).getRoleName()
        );

        verify(roleRepository)
                .findByTenantIdAndIsDeletedFalse(
                        "tenant-a",
                        pageable
                );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    @Test
    void searchRoles_shouldSearchByQuery() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository.searchRoles(
                "tenant-a",
                "HR",
                null,
                null
        )).thenReturn(List.of(role));

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        "HR",
                        null,
                        null
                );

        assertEquals(1, result.size());
        assertEquals(
                "HR Manager",
                result.get(0).getRoleName()
        );

        verify(roleRepository).searchRoles(
                "tenant-a",
                "HR",
                null,
                null
        );
    }

    @Test
    void searchRoles_shouldFilterByRoleType() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository.searchRoles(
                "tenant-a",
                null,
                RoleType.CUSTOM,
                null
        )).thenReturn(List.of(role));

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        null,
                        RoleType.CUSTOM,
                        null
                );

        assertEquals(1, result.size());
        assertEquals(
                RoleType.CUSTOM,
                result.get(0).getRoleType()
        );
    }

    @Test
    void searchRoles_shouldFilterByStatus() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository.searchRoles(
                "tenant-a",
                null,
                null,
                "ACTIVE"
        )).thenReturn(List.of(role));

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        null,
                        null,
                        "ACTIVE"
                );

        assertEquals(1, result.size());
        assertEquals(
                "ACTIVE",
                result.get(0).getStatus()
        );
    }

    @Test
    void searchRoles_shouldSupportAllFiltersTogether() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository.searchRoles(
                "tenant-a",
                "HR",
                RoleType.CUSTOM,
                "ACTIVE"
        )).thenReturn(List.of(role));

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        "HR",
                        RoleType.CUSTOM,
                        "ACTIVE"
                );

        assertEquals(1, result.size());
        assertEquals("HR Manager", result.get(0).getRoleName());
    }

    @Test
    void searchRoles_shouldReturnEmptyListWhenNoMatch() {

        when(roleRepository.searchRoles(
                "tenant-a",
                "xyz",
                null,
                null
        )).thenReturn(List.of());

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        "xyz",
                        null,
                        null
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Test
    void updateRole_shouldUpdateRoleNameAndDescription() {

        Role existingRole = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        RoleRequestDto request = new RoleRequestDto(
                "Senior HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "Updated description",
                "ACTIVE"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(existingRole));

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Senior HR Manager",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RoleResponseDto result =
                roleService.update(1L, request);

        assertEquals(
                "Senior HR Manager",
                result.getRoleName()
        );

        /*
         * The current production implementation only updates:
         * roleName and description.
         */
        assertEquals(
                "Updated description",
                result.getDescription()
        );

        assertEquals(
                "HR_MANAGER",
                result.getRoleCode()
        );

        verify(roleRepository).save(existingRole);
    }

    @Test
    void updateRole_shouldNotCheckDuplicateNameWhenNameIsUnchanged() {

        Role existingRole = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "Updated description",
                "ACTIVE"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(existingRole));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RoleResponseDto result =
                roleService.update(1L, request);

        assertEquals(
                "HR Manager",
                result.getRoleName()
        );

        verify(
                roleRepository,
                never()
        ).existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                anyString(),
                anyString()
        );

        verify(roleRepository).save(existingRole);
    }

    @Test
    void updateRole_shouldRejectDuplicateName() {

        Role existingRole = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        RoleRequestDto request = new RoleRequestDto(
                "Finance Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "Finance",
                "ACTIVE"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(existingRole));

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Finance Manager",
                        "tenant-a"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> roleService.update(1L, request)
        );

        verify(roleRepository, never())
                .save(any(Role.class));
    }

    @Test
    void updateRole_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.empty());

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "HR",
                "ACTIVE"
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.update(1L, request)
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Test
    void deleteRole_shouldSoftDeleteCustomRole() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        roleService.deleteById(1L);

        assertTrue(role.getIsDeleted());

        verify(roleRepository).save(role);
    }

    @Test
    void deleteRole_shouldRejectSuperAdminRole() {

        Role role = createRole(
                1L,
                "Super Admin",
                "SUPER_ADMIN",
                RoleType.SYSTEM,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.deleteById(1L)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }

    @Test
    void deleteRole_shouldRejectAdminRole() {

        Role role = createRole(
                1L,
                "Admin",
                "ADMIN",
                RoleType.SYSTEM,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.deleteById(1L)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }

    @Test
    void deleteRole_shouldRejectEmployeeRole() {

        Role role = createRole(
                1L,
                "Employee",
                "EMPLOYEE",
                RoleType.SYSTEM,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.deleteById(1L)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }

    // =========================================================
    // STATUS
    // =========================================================

    @Test
    void updateStatus_shouldActivateRole() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        role.setStatus("INACTIVE");

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RoleResponseDto result =
                roleService.updateStatus(
                        1L,
                        "ACTIVE"
                );

        assertEquals(
                "ACTIVE",
                result.getStatus()
        );
    }

    @Test
    void updateStatus_shouldDeactivateCustomRole() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RoleResponseDto result =
                roleService.updateStatus(
                        1L,
                        "INACTIVE"
                );

        assertEquals(
                "INACTIVE",
                result.getStatus()
        );
    }

    @Test
    void updateStatus_shouldRejectInvalidStatus() {

        assertThrows(
                BadRequestException.class,
                () -> roleService.updateStatus(
                        1L,
                        "INVALID"
                )
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }

    @Test
    void updateStatus_shouldNotDeactivateProtectedRole() {

        Role role = createRole(
                1L,
                "Admin",
                "ADMIN",
                RoleType.SYSTEM,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.updateStatus(
                        1L,
                        "INACTIVE"
                )
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }

    // =========================================================
    // ROLE COUNTS
    // =========================================================

    @Test
    void getRoleCounts_shouldReturnCorrectCounts() {

        when(roleRepository
                .countByTenantIdAndIsDeletedFalse(
                        "tenant-a"))
                .thenReturn(10L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-a",
                        RoleType.SYSTEM))
                .thenReturn(3L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-a",
                        RoleType.CUSTOM))
                .thenReturn(7L);

        Map<String, Long> result =
                roleService.getRoleCounts();

        assertEquals(
                10L,
                result.get("totalRoles")
        );

        assertEquals(
                3L,
                result.get("systemRoles")
        );

        assertEquals(
                7L,
                result.get("customRoles")
        );

        verify(roleRepository)
                .countByTenantIdAndIsDeletedFalse(
                        "tenant-a"
                );
    }

    @Test
    void getRoleCounts_shouldUseCurrentTenant() {

        TenantContext.setTenantId("tenant-b");

        when(roleRepository
                .countByTenantIdAndIsDeletedFalse(
                        "tenant-b"))
                .thenReturn(5L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-b",
                        RoleType.SYSTEM))
                .thenReturn(2L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-b",
                        RoleType.CUSTOM))
                .thenReturn(3L);

        Map<String, Long> result =
                roleService.getRoleCounts();

        assertEquals(
                5L,
                result.get("totalRoles")
        );

        assertEquals(
                2L,
                result.get("systemRoles")
        );

        assertEquals(
                3L,
                result.get("customRoles")
        );
    }

    // =========================================================
    // TENANT ISOLATION
    // =========================================================

    @Test
    void getById_shouldUseCurrentTenant() {

        TenantContext.setTenantId("tenant-b");

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-b"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getById(1L)
        );

        verify(roleRepository)
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-b"
                );

        verify(
                roleRepository,
                never()
        ).findByIdAndTenantIdAndIsDeletedFalse(
                1L,
                "tenant-a"
        );
    }

    @Test
    void clearedTenantContext_shouldUseDefaultTenant() {

        TenantContext.clear();

        assertEquals(
                TenantContext.DEFAULT_TENANT_ID,
                TenantContext.getTenantId()
        );

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(
                        TenantContext.DEFAULT_TENANT_ID))
                .thenReturn(List.of());

        List<RoleResponseDto> result =
                roleService.getAll();

        assertNotNull(result);

        verify(roleRepository)
                .findByTenantIdAndIsDeletedFalse(
                        TenantContext.DEFAULT_TENANT_ID
                );
    }

    // =========================================================
    // ROLE TEMPLATES
    // =========================================================

    @Test
    void listTemplates_shouldReturnVisibleTemplatesForNormalUser() {

        RoleTemplate template = createTemplate(
                "template-1",
                "HR Template",
                "HR template",
                "HR",
                false
        );

        when(currentUser.hasRole("SUPER_ADMIN"))
                .thenReturn(false);

        when(roleTemplateRepository.findAllByHiddenFalse())
                .thenReturn(List.of(template));

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                "template-1",
                result.get(0).id()
        );

        assertEquals(
                "HR Template",
                result.get(0).name()
        );

        assertEquals(
                "HR template",
                result.get(0).description()
        );

        assertEquals(
                0,
                result.get(0).permissionCount()
        );

        assertEquals(
                "HR",
                result.get(0).recommendedFor()
        );

        verify(roleTemplateRepository)
                .findAllByHiddenFalse();

        verify(
                roleTemplateRepository,
                never()
        ).findAll();
    }

    @Test
    void listTemplates_shouldReturnAllTemplatesForSuperAdmin() {

        RoleTemplate visibleTemplate = createTemplate(
                "template-1",
                "HR Template",
                "HR template",
                "HR",
                false
        );

        RoleTemplate hiddenTemplate = createTemplate(
                "template-2",
                "Hidden Template",
                "Hidden template",
                "Admin",
                true
        );

        when(currentUser.hasRole("SUPER_ADMIN"))
                .thenReturn(true);

        when(roleTemplateRepository.findAll())
                .thenReturn(
                        List.of(
                                visibleTemplate,
                                hiddenTemplate
                        )
                );

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertEquals(2, result.size());

        verify(roleTemplateRepository)
                .findAll();

        verify(
                roleTemplateRepository,
                never()
        ).findAllByHiddenFalse();
    }

    @Test
    void listTemplates_shouldReturnEmptyListWhenNoTemplatesExist() {

        when(currentUser.hasRole("SUPER_ADMIN"))
                .thenReturn(false);

        when(roleTemplateRepository.findAllByHiddenFalse())
                .thenReturn(List.of());

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTemplateDetail_shouldReturnTemplateDetails() {

        RoleTemplate template = createTemplate(
                "template-1",
                "HR Template",
                "HR template",
                "HR",
                false
        );

        when(roleTemplateRepository
                .findById("template-1"))
                .thenReturn(Optional.of(template));

        RoleTemplateDetailDto result =
                roleService.getTemplateDetail("template-1");

        assertNotNull(result);

        assertEquals(
                "template-1",
                result.id()
        );

        assertEquals(
                "HR Template",
                result.name()
        );

        assertEquals(
                "HR template",
                result.description()
        );

        assertTrue(
                result.permissionCodes().isEmpty()
        );

        assertEquals(
                "HR",
                result.recommendedFor()
        );
    }

    @Test
    void getTemplateDetail_shouldReturnPermissionCodes() {

        Permission permission1 = mock(Permission.class);
        Permission permission2 = mock(Permission.class);

        when(permission1.getPermissionCode())
                .thenReturn("ROLE_READ");

        when(permission2.getPermissionCode())
                .thenReturn("ROLE_UPDATE");

        RoleTemplate template = createTemplate(
                "template-1",
                "HR Template",
                "HR template",
                "HR",
                false
        );

        template.setPermissions(
                List.of(
                        permission1,
                        permission2
                )
        );

        when(roleTemplateRepository
                .findById("template-1"))
                .thenReturn(Optional.of(template));

        RoleTemplateDetailDto result =
                roleService.getTemplateDetail("template-1");

        assertEquals(
                Set.of(
                        "ROLE_READ",
                        "ROLE_UPDATE"
                ),
                result.permissionCodes()
        );
    }

    @Test
    void getTemplateDetail_shouldThrowWhenTemplateDoesNotExist() {

        when(roleTemplateRepository
                .findById("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getTemplateDetail("missing")
        );
    }

    // =========================================================
    // HELPER
    // =========================================================

    private Role createRole(
            Long id,
            String roleName,
            String roleCode,
            RoleType roleType,
            String tenantId) {

        Role role = new Role();

        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setRoleType(roleType);
        role.setDescription("Test role");
        role.setStatus("ACTIVE");
        role.setIsDeleted(false);
        role.setTenantId(tenantId);

        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setCreatedBy("test-user");
        role.setUpdatedBy("test-user");

        return role;
    }

    private RoleTemplate createTemplate(
            String id,
            String name,
            String description,
            String recommendedFor,
            boolean hidden) {

        RoleTemplate template = new RoleTemplate();

        template.setId(id);
        template.setName(name);
        template.setDescription(description);
        template.setRecommendedFor(recommendedFor);
        template.setHidden(hidden);
        template.setPermissions(List.of());

        return template;
    }
}