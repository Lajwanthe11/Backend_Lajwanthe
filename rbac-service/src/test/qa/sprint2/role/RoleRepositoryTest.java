package com.example.qa.sprint2.role;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import com.example.rbac.RbacApplication;
import org.springframework.test.context.ContextConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.RoleRepository;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {

        roleRepository.deleteAll();

        roleRepository.save(
                createRole(
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        "tenant-a",
                        false
                )
        );

        roleRepository.save(
                createRole(
                        "Admin",
                        "ADMIN",
                        RoleType.SYSTEM,
                        "ACTIVE",
                        "tenant-a",
                        false
                )
        );

        roleRepository.save(
                createRole(
                        "Finance Manager",
                        "FINANCE_MANAGER",
                        RoleType.CUSTOM,
                        "INACTIVE",
                        "tenant-a",
                        false
                )
        );

        roleRepository.save(
                createRole(
                        "HR Manager",
                        "HR_MANAGER_B",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        "tenant-b",
                        false
                )
        );

        roleRepository.save(
                createRole(
                        "Deleted Role",
                        "DELETED_ROLE",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        "tenant-a",
                        true
                )
        );
    }

    // =========================================================
    // DUPLICATE NAME
    // =========================================================

    @Test
    void existsByRoleName_shouldReturnTrueForSameTenant() {

        assertTrue(
                roleRepository
                        .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "HR Manager",
                                "tenant-a"
                        )
        );
    }

    @Test
    void existsByRoleName_shouldBeCaseInsensitive() {

        assertTrue(
                roleRepository
                        .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "hr manager",
                                "tenant-a"
                        )
        );
    }

    @Test
    void existsByRoleName_shouldReturnFalseForDifferentTenant() {

        assertFalse(
                roleRepository
                        .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "HR Manager",
                                "tenant-c"
                        )
        );
    }

    @Test
    void existsByRoleName_shouldIgnoreDeletedRole() {

        assertFalse(
                roleRepository
                        .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "Deleted Role",
                                "tenant-a"
                        )
        );
    }

    // =========================================================
    // DUPLICATE CODE
    // =========================================================

    @Test
    void existsByRoleCode_shouldReturnTrueForSameTenant() {

        assertTrue(
                roleRepository
                        .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "HR_MANAGER",
                                "tenant-a"
                        )
        );
    }

    @Test
    void existsByRoleCode_shouldBeCaseInsensitive() {

        assertTrue(
                roleRepository
                        .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "hr_manager",
                                "tenant-a"
                        )
        );
    }

    @Test
    void existsByRoleCode_shouldReturnFalseForDifferentTenant() {

        assertFalse(
                roleRepository
                        .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "HR_MANAGER",
                                "tenant-b"
                        )
        );
    }

    @Test
    void existsByRoleCode_shouldIgnoreDeletedRole() {

        assertFalse(
                roleRepository
                        .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "DELETED_ROLE",
                                "tenant-a"
                        )
        );
    }

    // =========================================================
    // FIND BY ID
    // =========================================================

    @Test
    void findById_shouldReturnRoleForCorrectTenant() {

        Role role =
                roleRepository
                        .findAll()
                        .stream()
                        .filter(r ->
                                "HR_MANAGER".equals(
                                        r.getRoleCode()))
                        .findFirst()
                        .orElseThrow();

        Optional<Role> result =
                roleRepository
                        .findByIdAndTenantIdAndIsDeletedFalse(
                                role.getId(),
                                "tenant-a"
                        );

        assertTrue(result.isPresent());

        assertEquals(
                "HR Manager",
                result.get().getRoleName()
        );
    }

    @Test
    void findById_shouldNotReturnRoleForWrongTenant() {

        Role role =
                roleRepository
                        .findAll()
                        .stream()
                        .filter(r ->
                                "HR_MANAGER".equals(
                                        r.getRoleCode()))
                        .findFirst()
                        .orElseThrow();

        Optional<Role> result =
                roleRepository
                        .findByIdAndTenantIdAndIsDeletedFalse(
                                role.getId(),
                                "tenant-b"
                        );

        assertTrue(result.isEmpty());
    }

    // =========================================================
    // GET ALL
    // =========================================================

    @Test
    void findByTenant_shouldReturnOnlyNonDeletedTenantRoles() {

        List<Role> roles =
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                "tenant-a"
                        );

        assertEquals(3, roles.size());

        assertTrue(
                roles.stream()
                        .noneMatch(Role::getIsDeleted)
        );

        assertTrue(
                roles.stream()
                        .allMatch(r ->
                                "tenant-a".equals(
                                        r.getTenantId()))
        );
    }

    @Test
    void findByTenant_shouldNotReturnOtherTenantRoles() {

        List<Role> roles =
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                "tenant-a"
                        );

        assertTrue(
                roles.stream()
                        .noneMatch(r ->
                                "tenant-b".equals(
                                        r.getTenantId()))
        );
    }

    // =========================================================
    // PAGINATION
    // =========================================================

    @Test
    void findByTenantPaged_shouldReturnPage() {

        PageRequest pageable =
                PageRequest.of(0, 2);

        Page<Role> result =
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                "tenant-a",
                                pageable
                        );

        assertEquals(
                2,
                result.getContent().size()
        );

        assertEquals(
                3,
                result.getTotalElements()
        );

        assertTrue(result.hasNext());
    }

    // =========================================================
    // SEARCH
    // =========================================================

    @Test
    void searchRoles_shouldFindByRoleName() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
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
    void searchRoles_shouldFindByRoleCode() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
                        "FINANCE",
                        null,
                        null
                );

        assertEquals(1, result.size());

        assertEquals(
                "FINANCE_MANAGER",
                result.get(0).getRoleCode()
        );
    }

    @Test
    void searchRoles_shouldBeCaseInsensitive() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
                        "hr manager",
                        null,
                        null
                );

        assertEquals(1, result.size());
    }

    @Test
    void searchRoles_shouldFilterByRoleType() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
                        null,
                        RoleType.SYSTEM,
                        null
                );

        assertEquals(1, result.size());

        assertEquals(
                RoleType.SYSTEM,
                result.get(0).getRoleType()
        );
    }

    @Test
    void searchRoles_shouldFilterByStatus() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
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
    void searchRoles_shouldApplyAllFilters() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
                        "HR",
                        RoleType.CUSTOM,
                        "ACTIVE"
                );

        assertEquals(1, result.size());

        assertEquals(
                "HR Manager",
                result.get(0).getRoleName()
        );
    }

    @Test
    void searchRoles_shouldNotReturnOtherTenantRoles() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
                        "HR",
                        null,
                        null
                );

        assertTrue(
                result.stream()
                        .allMatch(r ->
                                "tenant-a".equals(
                                        r.getTenantId()))
        );
    }

    @Test
    void searchRoles_shouldExcludeDeletedRoles() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
                        "Deleted",
                        null,
                        null
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void searchRoles_withNoFilters_shouldReturnTenantRoles() {

        List<Role> result =
                roleRepository.searchRoles(
                        "tenant-a",
                        null,
                        null,
                        null
                );

        assertEquals(3, result.size());
    }

    // =========================================================
    // COUNTS
    // =========================================================

    @Test
    void countByTenant_shouldReturnTotalActiveRoles() {

        long count =
                roleRepository
                        .countByTenantIdAndIsDeletedFalse(
                                "tenant-a"
                        );

        assertEquals(3, count);
    }

    @Test
    void countByTenantAndType_shouldCountSystemRoles() {

        long count =
                roleRepository
                        .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                                "tenant-a",
                                RoleType.SYSTEM
                        );

        assertEquals(1, count);
    }

    @Test
    void countByTenantAndType_shouldCountCustomRoles() {

        long count =
                roleRepository
                        .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                                "tenant-a",
                                RoleType.CUSTOM
                        );

        assertEquals(2, count);
    }

    @Test
    void counts_shouldNotIncludeDeletedRoles() {

        long count =
                roleRepository
                        .countByTenantIdAndIsDeletedFalse(
                                "tenant-a"
                        );

        assertEquals(3, count);
    }

    // =========================================================
    // HELPER
    // =========================================================

    private Role createRole(
            String roleName,
            String roleCode,
            RoleType roleType,
            String status,
            String tenantId,
            boolean deleted) {

        Role role = new Role();

        role.setRoleName(roleName);
        role.setRoleCode(roleCode);
        role.setRoleType(roleType);
        role.setStatus(status);
        role.setTenantId(tenantId);
        role.setIsDeleted(deleted);
        role.setDescription("Test role");

        return role;
    }
}