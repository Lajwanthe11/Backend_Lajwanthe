package com.example.platformadmin.rbac.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import com.example.platformadmin.rbac.exception.ResourceNotFoundException;

import com.example.platformadmin.rbac.dto.request.RoleRequestDto;
import com.example.platformadmin.rbac.dto.response.RoleResponseDto;
import com.example.platformadmin.rbac.dto.response.RoleTemplateDetailDto;
import com.example.platformadmin.rbac.dto.response.RoleTemplateSummaryDto;

import com.example.platformadmin.rbac.entity.Permission;
import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.entity.RoleTemplate;

import com.example.platformadmin.rbac.enums.RoleType;

import com.example.platformadmin.rbac.repository.RoleHistoryRepository;
import com.example.platformadmin.rbac.repository.RoleRepository;
import com.example.platformadmin.rbac.repository.RoleTemplateRepository;

import com.example.platformadmin.rbac.service.serviceImpl.RoleServiceImpl;


@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleTemplateRepository roleTemplateRepository;

    @Mock
    private CurrentUserContext currentUser;

    @Mock
    private RoleHistoryRepository roleHistoryRepository;

    @Mock
    private RoleExportService roleExportService;

    @InjectMocks
    private RoleServiceImpl roleService;


    private final UUID tenantId =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final UUID roleId =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final UUID secondRoleId =
            UUID.fromString("33333333-3333-3333-3333-333333333333");


    @BeforeEach
    void setUp() {

        /*
         * Developer implementation obtains tenant from:
         *
         * currentUser.getTenantId()
         *
         * and converts it using UUID.fromString().
         */
        lenient()
                .when(currentUser.getTenantId())
                .thenReturn(tenantId);
    }


    @AfterEach
    void tearDown() {
        // Nothing required.
    }


    // ============================================================
    // CREATE ROLE
    // ============================================================

    @Test
    void createRole_shouldCreateRoleSuccessfully() {

        RoleRequestDto request =
                new RoleRequestDto(
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "HR management role",
                        "ACTIVE"
                );

        Role savedRole =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        tenantId))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER",
                        tenantId))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result =
                roleService.create(request);

        assertNotNull(result);

        assertEquals(
                "HR Manager",
                result.getRoleName()
        );

        assertEquals(
                "HR_MANAGER",
                result.getRoleCode()
        );

        assertEquals(
                RoleType.CUSTOM,
                result.getRoleType()
        );

        assertEquals(
                "ACTIVE",
                result.getStatus()
        );

        verify(roleRepository)
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        tenantId
                );

        verify(roleRepository)
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER",
                        tenantId
                );

        verify(roleRepository)
                .save(any(Role.class));
    }


    @Test
    void createRole_shouldGenerateRoleCodeWhenCodeIsMissing() {

        RoleRequestDto request =
                new RoleRequestDto(
                        "HR Manager",
                        null,
                        RoleType.CUSTOM,
                        "HR role",
                        "ACTIVE"
                );

        Role savedRole =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        tenantId))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER",
                        tenantId))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result =
                roleService.create(request);

        assertNotNull(result);

        assertEquals(
                "HR_MANAGER",
                result.getRoleCode()
        );

        verify(roleRepository)
                .save(argThat(role ->
                        "HR_MANAGER".equals(
                                role.getRoleCode()
                        )
                ));
    }


    @Test
    void createRole_shouldGenerateRoleCodeFromSpecialCharacters() {

        RoleRequestDto request =
                new RoleRequestDto(
                        "Finance & Manager!",
                        null,
                        RoleType.CUSTOM,
                        "Finance role",
                        null
                );

        Role savedRole =
                createRole(
                        roleId,
                        "Finance & Manager!",
                        "FINANCE_MANAGER_",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Finance & Manager!",
                        tenantId))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "FINANCE_MANAGER_",
                        tenantId))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result =
                roleService.create(request);

        assertEquals(
                "FINANCE_MANAGER_",
                result.getRoleCode()
        );

        assertEquals(
                "ACTIVE",
                result.getStatus()
        );
    }


    @Test
    void createRole_shouldDefaultStatusToActive() {

        RoleRequestDto request =
                new RoleRequestDto(
                        "Finance Manager",
                        "FINANCE_MANAGER",
                        RoleType.CUSTOM,
                        "Finance role",
                        null
                );

        Role savedRole =
                createRole(
                        roleId,
                        "Finance Manager",
                        "FINANCE_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Finance Manager",
                        tenantId))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "FINANCE_MANAGER",
                        tenantId))
                .thenReturn(false);

        when(roleRepository.save(any(Role.class)))
                .thenReturn(savedRole);

        RoleResponseDto result =
                roleService.create(request);

        assertEquals(
                "ACTIVE",
                result.getStatus()
        );
    }


    @Test
    void createRole_shouldThrowWhenRoleNameAlreadyExists() {

        RoleRequestDto request =
                new RoleRequestDto(
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "HR role",
                        "ACTIVE"
                );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR Manager",
                        tenantId))
                .thenReturn(true);

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> roleService.create(request)
                );

        assertEquals(
                "Role name already exists",
                exception.getMessage()
        );

        verify(roleRepository, never())
                .save(any(Role.class));
    }


    @Test
    void createRole_shouldThrowWhenRoleCodeAlreadyExists() {

        RoleRequestDto request =
                new RoleRequestDto(
                        "Finance Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "Finance role",
                        "ACTIVE"
                );

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Finance Manager",
                        tenantId))
                .thenReturn(false);

        when(roleRepository
                .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "HR_MANAGER",
                        tenantId))
                .thenReturn(true);

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> roleService.create(request)
                );

        assertEquals(
                "Role code already exists",
                exception.getMessage()
        );

        verify(roleRepository, never())
                .save(any(Role.class));
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Test
    void getById_shouldReturnRole() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        RoleResponseDto result =
                roleService.getById(roleId);

        assertNotNull(result);

        assertEquals(
                roleId,
                result.getId()
        );

        assertEquals(
                "HR Manager",
                result.getRoleName()
        );

        assertEquals(
                "HR_MANAGER",
                result.getRoleCode()
        );

        assertEquals(
                RoleType.CUSTOM,
                result.getRoleType()
        );

        verify(roleRepository)
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId
                );
    }


    @Test
    void getById_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getById(roleId)
        );
    }


    @Test
    void getById_shouldNotReturnRoleFromAnotherTenant() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getById(roleId)
        );

        verify(roleRepository)
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId
                );
    }


    // ============================================================
    // GET ALL
    // ============================================================

    @Test
    void getAll_shouldReturnCurrentTenantRoles() {

        Role role1 =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        Role role2 =
                createRole(
                        secondRoleId,
                        "Finance Manager",
                        "FINANCE_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(tenantId))
                .thenReturn(List.of(role1, role2));

        List<RoleResponseDto> result =
                roleService.getAll();

        assertNotNull(result);
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
                .findByTenantIdAndIsDeletedFalse(
                        tenantId
                );
    }


    @Test
    void getAll_shouldReturnEmptyListWhenNoRolesExist() {

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(tenantId))
                .thenReturn(List.of());

        List<RoleResponseDto> result =
                roleService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // ============================================================
    // PAGINATION
    // ============================================================

    @Test
    void getAll_withPagination_shouldReturnPage() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Role> rolePage =
                new PageImpl<>(
                        List.of(role),
                        pageable,
                        1
                );

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(
                        tenantId,
                        pageable))
                .thenReturn(rolePage);

        Page<RoleResponseDto> result =
                roleService.getAll(pageable);

        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                1,
                result.getContent().size()
        );

        assertEquals(
                "HR Manager",
                result.getContent()
                        .get(0)
                        .getRoleName()
        );
    }


    // ============================================================
    // SEARCH
    // ============================================================

    @Test
    void searchRoles_shouldSearchByQuery() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository.searchRoles(
                tenantId,
                "HR",
                null,
                null))
                .thenReturn(List.of(role));

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
    }


    @Test
    void searchRoles_shouldFilterByRoleType() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository.searchRoles(
                tenantId,
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

        assertEquals(
                RoleType.CUSTOM,
                result.get(0).getRoleType()
        );
    }


    @Test
    void searchRoles_shouldFilterByStatus() {

        Role role =
                createRole(
                        roleId,
                        "Finance Manager",
                        "FINANCE_MANAGER",
                        RoleType.CUSTOM,
                        "INACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository.searchRoles(
                tenantId,
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

        assertEquals(
                "INACTIVE",
                result.get(0).getStatus()
        );
    }


    @Test
    void searchRoles_shouldSupportAllFilters() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository.searchRoles(
                tenantId,
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

        verify(roleRepository)
                .searchRoles(
                        tenantId,
                        "HR",
                        RoleType.CUSTOM,
                        "ACTIVE"
                );
    }


    @Test
    void searchRoles_shouldReturnEmptyWhenNoMatch() {

        when(roleRepository.searchRoles(
                tenantId,
                "XYZ",
                null,
                null))
                .thenReturn(List.of());

        List<RoleResponseDto> result =
                roleService.searchRoles(
                        "XYZ",
                        null,
                        null
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    // ============================================================
    // UPDATE
    // ============================================================

    @Test
    void update_shouldUpdateRoleNameAndDescription() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        RoleRequestDto request =
                new RoleRequestDto(
                        "Senior HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "Updated HR description",
                        "ACTIVE"
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        RoleResponseDto result =
                roleService.update(
                        roleId,
                        request
                );

        assertNotNull(result);

        assertEquals(
                "Senior HR Manager",
                result.getRoleName()
        );

        assertEquals(
                "Updated HR description",
                result.getDescription()
        );

        // Code is immutable during update.
        assertEquals(
                "HR_MANAGER",
                result.getRoleCode()
        );

        verify(roleRepository)
                .save(role);
    }


    @Test
    void update_shouldNotCheckDuplicateWhenNameIsUnchanged() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        RoleRequestDto request =
                new RoleRequestDto(
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "Updated description",
                        "ACTIVE"
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        roleService.update(
                roleId,
                request
        );

        verify(
                roleRepository,
                never()
        ).existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                anyString(),
                eq(tenantId)
        );
    }


    @Test
    void update_shouldThrowWhenNewRoleNameAlreadyExists() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        RoleRequestDto request =
                new RoleRequestDto(
                        "Finance Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "Finance role",
                        "ACTIVE"
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        when(roleRepository
                .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                        "Finance Manager",
                        tenantId))
                .thenReturn(true);

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> roleService.update(
                                roleId,
                                request
                        )
                );

        assertEquals(
                "Role name already exists",
                exception.getMessage()
        );

        verify(roleRepository, never())
                .save(any(Role.class));
    }


    @Test
    void update_shouldThrowWhenRoleDoesNotExist() {

        RoleRequestDto request =
                new RoleRequestDto(
                        "Updated Role",
                        "UPDATED_ROLE",
                        RoleType.CUSTOM,
                        "Updated",
                        "ACTIVE"
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.update(
                        roleId,
                        request
                )
        );

        verify(roleRepository, never())
                .save(any(Role.class));
    }


    // ============================================================
    // DELETE
    // ============================================================

    @Test
    void deleteById_shouldSoftDeleteCustomRole() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        roleService.deleteById(roleId);

        assertTrue(role.getIsDeleted());

        assertNotNull(role.getDeletedAt());

        verify(roleRepository)
                .save(role);
    }


    @Test
    void deleteById_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.deleteById(roleId)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void deleteById_shouldRejectSuperAdminRole() {

        Role role =
                createRole(
                        roleId,
                        "Super Administrator",
                        "SUPER_ADMIN",
                        RoleType.SYSTEM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> roleService.deleteById(roleId)
                );

        assertEquals(
                "System role cannot be deleted",
                exception.getMessage()
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void deleteById_shouldRejectAdminRole() {

        Role role =
                createRole(
                        roleId,
                        "Administrator",
                        "ADMIN",
                        RoleType.SYSTEM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.deleteById(roleId)
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void deleteById_shouldRejectEmployeeRole() {

        Role role =
                createRole(
                        roleId,
                        "Employee",
                        "EMPLOYEE",
                        RoleType.SYSTEM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.deleteById(roleId)
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

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "INACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        RoleResponseDto result =
                roleService.updateStatus(
                        roleId,
                        "ACTIVE"
                );

        assertEquals(
                "ACTIVE",
                result.getStatus()
        );

        assertEquals(
                "ACTIVE",
                role.getStatus()
        );

        verify(roleRepository)
                .save(role);
    }


    @Test
    void updateStatus_shouldDeactivateCustomRole() {

        Role role =
                createRole(
                        roleId,
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        when(roleRepository.save(any(Role.class)))
                .thenReturn(role);

        RoleResponseDto result =
                roleService.updateStatus(
                        roleId,
                        "INACTIVE"
                );

        assertEquals(
                "INACTIVE",
                result.getStatus()
        );

        assertEquals(
                "INACTIVE",
                role.getStatus()
        );

        verify(roleRepository)
                .save(role);
    }


    @Test
    void updateStatus_shouldRejectProtectedSystemRoleDeactivation() {

        Role role =
                createRole(
                        roleId,
                        "Super Administrator",
                        "SUPER_ADMIN",
                        RoleType.SYSTEM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        BadRequestException exception =
                assertThrows(
                        BadRequestException.class,
                        () -> roleService.updateStatus(
                                roleId,
                                "INACTIVE"
                        )
                );

        assertEquals(
                "System role cannot be deactivated",
                exception.getMessage()
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void updateStatus_shouldRejectAdminRoleDeactivation() {

        Role role =
                createRole(
                        roleId,
                        "Administrator",
                        "ADMIN",
                        RoleType.SYSTEM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.updateStatus(
                        roleId,
                        "INACTIVE"
                )
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void updateStatus_shouldRejectEmployeeRoleDeactivation() {

        Role role =
                createRole(
                        roleId,
                        "Employee",
                        "EMPLOYEE",
                        RoleType.SYSTEM,
                        "ACTIVE",
                        false,
                        tenantId
                );

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.of(role));

        assertThrows(
                BadRequestException.class,
                () -> roleService.updateStatus(
                        roleId,
                        "INACTIVE"
                )
        );

        verify(
                roleRepository,
                never()
        ).save(any(Role.class));
    }


    @Test
    void updateStatus_shouldRejectInvalidStatus() {

        assertThrows(
                BadRequestException.class,
                () -> roleService.updateStatus(
                        roleId,
                        "INVALID"
                )
        );

        verify(
                roleRepository,
                never()
        ).findByIdAndTenantIdAndIsDeletedFalse(
                any(UUID.class),
                eq(tenantId)
        );
    }


    @Test
    void updateStatus_shouldRejectNullStatus() {

        assertThrows(
                BadRequestException.class,
                () -> roleService.updateStatus(
                        roleId,
                        null
                )
        );

        verify(
                roleRepository,
                never()
        ).findByIdAndTenantIdAndIsDeletedFalse(
                any(UUID.class),
                eq(tenantId)
        );
    }


    @Test
    void updateStatus_shouldThrowWhenRoleDoesNotExist() {

        when(roleRepository
                .findByIdAndTenantIdAndIsDeletedFalse(
                        roleId,
                        tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.updateStatus(
                        roleId,
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
                .countByTenantIdAndIsDeletedFalse(
                        tenantId))
                .thenReturn(5L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        tenantId,
                        RoleType.SYSTEM))
                .thenReturn(2L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        tenantId,
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
                .countByTenantIdAndIsDeletedFalse(
                        tenantId))
                .thenReturn(10L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        tenantId,
                        RoleType.SYSTEM))
                .thenReturn(4L);

        when(roleRepository
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        tenantId,
                        RoleType.CUSTOM))
                .thenReturn(6L);

        roleService.getRoleCounts();

        verify(roleRepository)
                .countByTenantIdAndIsDeletedFalse(
                        tenantId
                );

        verify(roleRepository)
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        tenantId,
                        RoleType.SYSTEM
                );

        verify(roleRepository)
                .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                        tenantId,
                        RoleType.CUSTOM
                );
    }


    // ============================================================
    // ROLE TEMPLATES
    // ============================================================

    @Test
    void listTemplates_shouldReturnVisibleTemplatesForNormalUser() {

        RoleTemplate template =
                mock(RoleTemplate.class);

        UUID templateId = UUID.randomUUID();
        when(template.getId())
                .thenReturn(templateId);

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

        when(roleTemplateRepository
                .findAllByHiddenFalse())
                .thenReturn(List.of(template));

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                templateId,
                result.get(0).getId()
        );

        assertEquals(
                "HR Manager",
                result.get(0).getName()
        );

        assertEquals(
                0,
                result.get(0).getPermissionCount()
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

        UUID templateId = UUID.randomUUID();

        when(template.getId())
                .thenReturn(templateId);

        when(template.getName())
                .thenReturn("Admin");

        when(template.getDescription())
                .thenReturn("Admin template");

        when(template.getRecommendedFor())
                .thenReturn("Administration");

        Permission permission = new Permission();
        permission.setPermissionCode("ROLE_READ");

        when(template.getPermissions())
                .thenReturn(Set.of(permission));

        when(currentUser.hasRole("SUPER_ADMIN"))
                .thenReturn(true);

        when(roleTemplateRepository.findAllWithPermissions())
                .thenReturn(List.of(template));

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void listTemplates_shouldReturnEmptyWhenNoTemplatesExist() {

        when(currentUser.hasRole("SUPER_ADMIN"))
                .thenReturn(false);

        when(roleTemplateRepository
                .findAllByHiddenFalse())
                .thenReturn(List.of());

        List<RoleTemplateSummaryDto> result =
                roleService.listTemplates();

        assertTrue(result.isEmpty());
    }


    // ============================================================
    // TEMPLATE DETAIL
    // ============================================================

    @Test
    void getTemplateDetail_shouldReturnTemplateWithPermissionCodes() {

        RoleTemplate template =
                mock(RoleTemplate.class);

        Permission permission1 =
                mock(Permission.class);

        Permission permission2 =
                mock(Permission.class);

        when(permission1.getPermissionCode())
                .thenReturn("USER_READ");

        when(permission2.getPermissionCode())
                .thenReturn("USER_WRITE");

        UUID templateId = UUID.randomUUID();
        when(template.getId())
                .thenReturn(templateId);

        when(template.getName())
                .thenReturn("HR Manager");

        when(template.getDescription())
                .thenReturn("HR template");

        when(template.getRecommendedFor())
                .thenReturn("HR");

        when(template.getPermissions())
                .thenReturn(
                        Set.of(
                                permission1,
                                permission2
                        )
                );

        when(roleTemplateRepository
                .findById(templateId))
                .thenReturn(Optional.of(template));

        RoleTemplateDetailDto result =
                roleService.getTemplateDetail(
                        templateId
                );

        assertNotNull(result);

        assertEquals(
                templateId,
                result.getId()
        );

        assertEquals(
                "HR Manager",
                result.getName()
        );

        assertEquals(
                2,
                result.getPermissionCodes().size()
        );

        assertTrue(
                result.getPermissionCodes()
                        .contains("USER_READ")
        );

        assertTrue(
                result.getPermissionCodes()
                        .contains("USER_WRITE")
        );
    }


    @Test
    void getTemplateDetail_shouldThrowWhenTemplateDoesNotExist() {

        UUID unknownId = UUID.randomUUID();
        when(roleTemplateRepository
                .findById(unknownId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roleService.getTemplateDetail(
                        unknownId
                )
        );
    }


    // ============================================================
    // TENANT ISOLATION
    // ============================================================

    @Test
    void getAll_shouldOnlyUseCurrentTenant() {

        when(roleRepository
                .findByTenantIdAndIsDeletedFalse(
                        tenantId))
                .thenReturn(List.of());

        roleService.getAll();

        verify(roleRepository)
                .findByTenantIdAndIsDeletedFalse(
                        tenantId
                );

        verify(
                roleRepository,
                never()
        ).findByTenantIdAndIsDeletedFalse(
                argThat(otherTenant ->
                        otherTenant != null
                                && !otherTenant.equals(tenantId)
                )
        );
    }


    @Test
    void searchRoles_shouldOnlyUseCurrentTenant() {

        when(roleRepository.searchRoles(
                tenantId,
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
                        tenantId,
                        null,
                        null,
                        null
                );
    }


    // ============================================================
    // HELPER
    // ============================================================

    private Role createRole(
            UUID id,
            String roleName,
            String roleCode,
            RoleType roleType,
            String status,
            Boolean isDeleted,
            UUID tenantId
    ) {

        Role role = new Role();

        role.setId(id);
        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setRoleType(roleType);
        role.setStatus(status);
        role.setIsDeleted(isDeleted);
        role.setTenantId(tenantId);
        role.setDescription(
                roleName + " description"
        );

        return role;
    }
}