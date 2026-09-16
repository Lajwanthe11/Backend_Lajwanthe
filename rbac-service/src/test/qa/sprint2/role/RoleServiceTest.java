package com.example.qa.sprint2.role;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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


    // ============================================================
    // CREATE ROLE
    // ============================================================

    @Test
    void createRole_shouldCreateRoleSuccessfully() {

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "HR management role",
                "ACTIVE"
        );

        Role savedRole = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result = roleService.create(request);

        assertNotNull(result);
        assertEquals("HR Manager", result.getRoleName());
        assertEquals("HR_MANAGER", result.getRoleCode());
        assertEquals(RoleType.CUSTOM, result.getRoleType());
        assertEquals("ACTIVE", result.getStatus());

        verify(roleRepository).save(any(Role.class));
    }


    @Test
    void createRole_shouldGenerateRoleCodeWhenCodeIsMissing() {

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                null,
                RoleType.CUSTOM,
                "HR role",
                "ACTIVE"
        );

        Role savedRole = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result = roleService.create(request);

        assertNotNull(result);
        assertEquals("HR_MANAGER", result.getRoleCode());

        verify(roleRepository).save(
                argThat(role ->
                        "HR_MANAGER".equals(role.getRoleCode())
                )
        );
    }


    @Test
    void createRole_shouldGenerateRoleCodeFromSpecialCharacters() {

        RoleRequestDto request = new RoleRequestDto(
                "Finance Manager",
                null,
                RoleType.CUSTOM,
                "Finance role",
                null
        );

        Role savedRole = createRole(
                2L,
                "Finance Manager",
                "FINANCE_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Finance Manager",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "FINANCE_MANAGER",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result = roleService.create(request);

        assertEquals("FINANCE_MANAGER", result.getRoleCode());
        assertEquals("ACTIVE", result.getStatus());
    }


    @Test
    void createRole_shouldThrowWhenRoleNameAlreadyExists() {

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "HR role",
                "ACTIVE"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        "tenant-a"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> roleService.create(request)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }


    @Test
    void createRole_shouldThrowWhenRoleCodeAlreadyExists() {

        RoleRequestDto request = new RoleRequestDto(
                "Finance Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "Finance role",
                "ACTIVE"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Finance Manager",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER",
                        "tenant-a"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> roleService.create(request)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }


    @Test
    void createRole_shouldAllowSameRoleNameInDifferentTenant() {

        RoleRequestDto request = new RoleRequestDto(
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "HR role",
                "ACTIVE"
        );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        "tenant-a"))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER",
                        "tenant-a"))
                .thenReturn(false);

        Role savedRole = createRole(
                5L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result = roleService.create(request);

        assertNotNull(result);

        verify(roleRepository)
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        "tenant-a"
                );
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Test
    void getById_shouldReturnRole() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        RoleResponseDto result = roleService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("HR Manager", result.getRoleName());
        assertEquals("HR_MANAGER", result.getRoleCode());
        assertEquals(RoleType.CUSTOM, result.getRoleType());
    }


    @Test
    void getById_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        99L,
                        "tenant-a"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getById(99L)
        );
    }


    @Test
    void getById_shouldNotReturnRoleFromAnotherTenant() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getById(1L)
        );
    }


    // ============================================================
    // GET ALL
    // ============================================================

    @Test
    void getAll_shouldReturnCurrentTenantRoles() {

        Role role1 = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        Role role2 = createRole(
                2L,
                "Finance Manager",
                "FINANCE_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse("tenant-a"))
                .thenReturn(List.of(role1, role2));

        List<RoleResponseDto> result = roleService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("HR Manager", result.get(0).getRoleName());
        assertEquals("Finance Manager", result.get(1).getRoleName());
    }


    @Test
    void getAll_shouldReturnEmptyListWhenNoRolesExist() {

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse("tenant-a"))
                .thenReturn(List.of());

        List<RoleResponseDto> result = roleService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // ============================================================
    // PAGINATION
    // ============================================================

    @Test
    void getAll_withPagination_shouldReturnPage() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        Pageable pageable = PageRequest.of(0, 10);

        Page<Role> rolePage =
                new PageImpl<>(List.of(role), pageable, 1);

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(
                        "tenant-a",
                        pageable))
                .thenReturn(rolePage);

        Page<RoleResponseDto> result =
                roleService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(
                "HR Manager",
                result.getContent().get(0).getRoleName()
        );
    }


    // ============================================================
    // SEARCH
    // ============================================================

    @Test
    void searchRoles_shouldSearchByQuery() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository.searchRoles(
                "tenant-a",
                "HR",
                null,
                null))
                .thenReturn(List.of(role));

        List<RoleResponseDto> result =
                roleService.searchRoles("HR", null, null);

        assertEquals(1, result.size());
        assertEquals("HR Manager", result.get(0).getRoleName());
    }


    @Test
    void searchRoles_shouldFilterByRoleType() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository.searchRoles(
                "tenant-a",
                null,
                RoleType.CUSTOM,
                null))
                .thenReturn(List.of(role));

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        null,
                        RoleType.CUSTOM,
                        null
                );

        assertEquals(1, result.size());
        assertEquals(RoleType.CUSTOM, result.get(0).getRoleType());
    }


    @Test
    void searchRoles_shouldFilterByStatus() {

        Role role = createRole(
                1L,
                "Finance Manager",
                "FINANCE_MANAGER",
                RoleType.CUSTOM,
                "INACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository.searchRoles(
                "tenant-a",
                null,
                null,
                "INACTIVE"))
                .thenReturn(List.of(role));

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        null,
                        null,
                        "INACTIVE"
                );

        assertEquals(1, result.size());
        assertEquals("INACTIVE", result.get(0).getStatus());
    }


    @Test
    void searchRoles_shouldSupportAllFilters() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository.searchRoles(
                "tenant-a",
                "HR",
                RoleType.CUSTOM,
                "ACTIVE"))
                .thenReturn(List.of(role));

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        "HR",
                        RoleType.CUSTOM,
                        "ACTIVE"
                );

        assertEquals(1, result.size());

        verify(roleRepository).searchRoles(
                "tenant-a",
                "HR",
                RoleType.CUSTOM,
                "ACTIVE"
        );
    }


    @Test
    void searchRoles_shouldReturnEmptyWhenNoMatch() {

        when(roleRepository.searchRoles(
                "tenant-a",
                "XYZ",
                null,
                null))
                .thenReturn(List.of());

        List<RoleResponseDto> result =
                roleService.searchRoles("XYZ", null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // ============================================================
    // UPDATE
    // ============================================================

    @Test
    void update_shouldUpdateRoleNameAndDescription() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        RoleRequestDto request = new RoleRequestDto(
                "Senior HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "Updated HR description",
                "ACTIVE"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        RoleResponseDto result =
                roleService.update(1L, request);

        assertNotNull(result);
        assertEquals(
                "Senior HR Manager",
                result.getRoleName()
        );

        assertEquals(
                "Updated HR description",
                result.getDescription()
        );

        verify(roleRepository).save(role);
    }


    @Test
    void update_shouldNotCheckDuplicateWhenNameIsUnchanged() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
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
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        roleService.update(1L, request);

        verify(
                roleRepository,
                never()
        ).existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                anyString(),
                anyString()
        );
    }


    @Test
    void update_shouldThrowWhenNewRoleNameAlreadyExists() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        RoleRequestDto request = new RoleRequestDto(
                "Finance Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "Finance role",
                "ACTIVE"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Finance Manager",
                        "tenant-a"))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> roleService.update(1L, request)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void update_shouldThrowWhenRoleDoesNotExist() {

        RoleRequestDto request = new RoleRequestDto(
                "Updated Role",
                "UPDATED_ROLE",
                RoleType.CUSTOM,
                "Updated",
                "ACTIVE"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        99L,
                        "tenant-a"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.update(99L, request)
        );

        verify(roleRepository, never()).save(any(Role.class));
    }


    // ============================================================
    // DELETE
    // ============================================================

    @Test
    void deleteById_shouldSoftDeleteCustomRole() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        roleService.deleteById(1L);

        assertTrue(role.getIsDeleted());

        verify(roleRepository).save(role);
    }


    @Test
    void deleteById_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        99L,
                        "tenant-a"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.deleteById(99L)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void deleteById_shouldRejectSuperAdminRole() {

        Role role = createRole(
                1L,
                "Super Administrator",
                "SUPER_ADMIN",
                RoleType.SYSTEM,
                "ACTIVE",
                false,
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
    void deleteById_shouldRejectAdminRole() {

        Role role = createRole(
                2L,
                "Administrator",
                "ADMIN",
                RoleType.SYSTEM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        2L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.deleteById(2L)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void deleteById_shouldRejectEmployeeRole() {

        Role role = createRole(
                3L,
                "Employee",
                "EMPLOYEE",
                RoleType.SYSTEM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        3L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.deleteById(3L)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    // ============================================================
    // STATUS
    // ============================================================

    @Test
    void updateStatus_shouldActivateRole() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "INACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        RoleResponseDto result =
                roleService.updateStatus(1L, "ACTIVE");

        assertEquals("ACTIVE", result.getStatus());
        assertEquals("ACTIVE", role.getStatus());

        verify(roleRepository).save(role);
    }


    @Test
    void updateStatus_shouldDeactivateRole() {

        Role role = createRole(
                1L,
                "HR Manager",
                "HR_MANAGER",
                RoleType.CUSTOM,
                "ACTIVE",
                false,
                "tenant-a"
        );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        1L,
                        "tenant-a"))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        RoleResponseDto result =
                roleService.updateStatus(1L, "INACTIVE");

        assertEquals("INACTIVE", result.getStatus());
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
        ).findByIdAndTenantIdAndIsDeletedFalse(
                anyLong(),
                anyString()
        );
    }


    @Test
    void updateStatus_shouldRejectNullStatus() {

        assertThrows(
                BadRequestException.class,
                () -> roleService.updateStatus(
                        1L,
                        null
                )
        );

        verify(
                roleRepository,
                never()
        ).findByIdAndTenantIdAndIsDeletedFalse(
                anyLong(),
                anyString()
        );
    }


    @Test
    void updateStatus_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        99L,
                        "tenant-a"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.updateStatus(
                        99L,
                        "ACTIVE"
                )
        );
    }


    // ============================================================
    // ROLE COUNTS
    // ============================================================

    @Test
    void getRoleCounts_shouldReturnCorrectCounts() {

        when(roleRepository
                .countByTenantIdAndIsDeletedFalse("tenant-a"))
                .thenReturn(5L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-a",
                        RoleType.SYSTEM))
                .thenReturn(2L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-a",
                        RoleType.CUSTOM))
                .thenReturn(3L);

        Map<String, Long> result =
                roleService.getRoleCounts();

        assertNotNull(result);

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


    @Test
    void getRoleCounts_shouldUseCurrentTenant() {

        when(roleRepository
                .countByTenantIdAndIsDeletedFalse("tenant-a"))
                .thenReturn(10L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-a",
                        RoleType.SYSTEM))
                .thenReturn(4L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-a",
                        RoleType.CUSTOM))
                .thenReturn(6L);

        roleService.getRoleCounts();

        verify(roleRepository)
                .countByTenantIdAndIsDeletedFalse("tenant-a");

        verify(roleRepository)
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-a",
                        RoleType.SYSTEM
                );

        verify(roleRepository)
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        "tenant-a",
                        RoleType.CUSTOM
                );
    }


    // ============================================================
    // ROLE TEMPLATE LIBRARY
    // ============================================================

    @Test
    void listTemplates_shouldReturnVisibleTemplatesForNormalUser() {

        RoleTemplate template = mock(RoleTemplate.class);

        when(template.getId())
                .thenReturn("HR_TEMPLATE");

        when(template.getName())
                .thenReturn("HR Manager");

        when(template.getDescription())
                .thenReturn("HR template");

        when(template.getRecommendedFor())
                .thenReturn("HR");

        when(template.getPermissions())
                .thenReturn(Set.of());

        when(currentUser.hasRole("SUPER_ADMIN"))
                .thenReturn(false);

        when(roleTemplateRepository.findAllByHiddenFalse())
                .thenReturn(List.of(template));

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                "HR_TEMPLATE",
                result.get(0).id()
        );

        assertEquals(
                "HR Manager",
                result.get(0).name()
        );

        assertEquals(
                0,
                result.get(0).permissionCount()
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

        RoleTemplate template = mock(RoleTemplate.class);

        when(template.getId())
                .thenReturn("ADMIN_TEMPLATE");

        when(template.getName())
                .thenReturn("Admin");

        when(template.getDescription())
                .thenReturn("Admin template");

        when(template.getRecommendedFor())
                .thenReturn("Administration");

        when(template.getPermissions())
                .thenReturn(Set.of());

        when(currentUser.hasRole("SUPER_ADMIN"))
                .thenReturn(true);

        when(roleTemplateRepository.findAll())
                .thenReturn(List.of(template));

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(roleTemplateRepository).findAll();

        verify(
                roleTemplateRepository,
                never()
        ).findAllByHiddenFalse();
    }


    @Test
    void listTemplates_shouldReturnEmptyWhenNoTemplatesExist() {

        when(currentUser.hasRole("SUPER_ADMIN"))
                .thenReturn(false);

        when(roleTemplateRepository.findAllByHiddenFalse())
                .thenReturn(List.of());

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // ============================================================
    // TEMPLATE DETAIL
    // ============================================================

    @Test
    void getTemplateDetail_shouldReturnTemplateWithPermissionCodes() {

        RoleTemplate template = mock(RoleTemplate.class);

        Permission permission1 = mock(Permission.class);
        Permission permission2 = mock(Permission.class);

        when(permission1.getPermissionCode())
                .thenReturn("USER_READ");

        when(permission2.getPermissionCode())
                .thenReturn("USER_WRITE");

        when(template.getId())
                .thenReturn("HR_TEMPLATE");

        when(template.getName())
                .thenReturn("HR Manager");

        when(template.getDescription())
                .thenReturn("HR template");

        when(template.getRecommendedFor())
                .thenReturn("HR");

        when(template.getPermissions())
                .thenReturn(Set.of(
                        permission1,
                        permission2
                ));

        when(roleTemplateRepository
                .findById("HR_TEMPLATE"))
                .thenReturn(Optional.of(template));

        RoleTemplateDetailDto result =
                roleService.getTemplateDetail("HR_TEMPLATE");

        assertNotNull(result);

        assertEquals(
                "HR_TEMPLATE",
                result.id()
        );

        assertEquals(
                "HR Manager",
                result.name()
        );

        assertEquals(
                2,
                result.permissionCodes().size()
        );

        assertTrue(
                result.permissionCodes()
                        .contains("USER_READ")
        );

        assertTrue(
                result.permissionCodes()
                        .contains("USER_WRITE")
        );
    }


    @Test
    void getTemplateDetail_shouldThrowWhenTemplateDoesNotExist() {

        when(roleTemplateRepository
                .findById("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getTemplateDetail("UNKNOWN")
        );
    }


    // ============================================================
    // TENANT ISOLATION
    // ============================================================

    @Test
    void getAll_shouldOnlyUseCurrentTenant() {

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse("tenant-a"))
                .thenReturn(List.of());

        roleService.getAll();

        verify(roleRepository)
                .findByTenantIdAndIsDeletedFalse(
                        "tenant-a"
                );

        verify(
                roleRepository,
                never()
        ).findByTenantIdAndIsDeletedFalse(
                eq("tenant-b")
        );
    }


    @Test
    void searchRoles_shouldOnlyUseCurrentTenant() {

        when(roleRepository.searchRoles(
                "tenant-a",
                null,
                null,
                null))
                .thenReturn(List.of());

        roleService.searchRoles(
                null,
                null,
                null
        );

        verify(roleRepository)
                .searchRoles(
                        "tenant-a",
                        null,
                        null,
                        null
                );
    }


    // ============================================================
    // HELPER METHOD
    // ============================================================

    private Role createRole(
            Long id,
            String roleName,
            String roleCode,
            RoleType roleType,
            String status,
            Boolean isDeleted,
            String tenantId
    ) {

        Role role = new Role();

        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setRoleType(roleType);
        role.setStatus(status);
        role.setIsDeleted(isDeleted);
        role.setTenantId(tenantId);
        role.setDescription(roleName + " description");

        return role;
    }
}