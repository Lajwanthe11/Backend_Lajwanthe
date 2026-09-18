package com.example.rbac.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;

import org.springframework.context.annotation.Import;
import com.example.common.config.TenantIdentifierResolver;

 //Repository-level tests for RoleRepository.
 //Verifies:tenant isolation,duplicate role-name detection,duplicate role-code detection,soft-delete handling,
 //role lookup by ID and tenant,tenant-specific role retrieval, pagination,role searching, role counts
@DataJpaTest
@Import(TenantIdentifierResolver.class)
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private UUID tenantA;
    private UUID tenantB;
    private UUID tenantC;

    /**
     * Creates representative roles for multiple tenants and states.
     *
     * Test data includes custom/system roles, active/inactive roles,
     * multiple tenants, and a soft-deleted role.
     */
    @BeforeEach
    void setUp() {

        roleRepository.deleteAll();

        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();
        tenantC = UUID.randomUUID();

        // Active custom role for tenant A.
        roleRepository.save(
                createRole(
                        "HR Manager",
                        "HR_MANAGER",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        tenantA,
                        false
                )
        );

        // Active system role for tenant A.
        roleRepository.save(
                createRole(
                        "Admin",
                        "ADMIN",
                        RoleType.SYSTEM,
                        "ACTIVE",
                        tenantA,
                        false
                )
        );

        // Inactive custom role for tenant A.
        roleRepository.save(
                createRole(
                        "Finance Manager",
                        "FINANCE_MANAGER",
                        RoleType.CUSTOM,
                        "INACTIVE",
                        tenantA,
                        false
                )
        );

        // Role with the same display name as the tenant-A HR role,
        // but belonging to a different tenant.
        roleRepository.save(
                createRole(
                        "HR Manager",
                        "HR_MANAGER_B",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        tenantB,
                        false
                )
        );

        // Soft-deleted role for tenant A.
        roleRepository.save(
                createRole(
                        "Deleted Role",
                        "DELETED_ROLE",
                        RoleType.CUSTOM,
                        "ACTIVE",
                        tenantA,
                        true
                )
        );
    }

    // =========================================================
    // DUPLICATE NAME
    // =========================================================

    /**
     * Verifies that an existing role name is detected
     * within the same tenant.
     */
    @Test
    void existsByRoleName_shouldReturnTrueForSameTenant() {

        assertTrue(
                roleRepository
                        .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "HR Manager",
                                tenantA
                        )
        );
    }

    /**
     * Verifies that role-name duplicate detection
     * is case-insensitive.
     */
    @Test
    void existsByRoleName_shouldBeCaseInsensitive() {

        assertTrue(
                roleRepository
                        .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "hr manager",
                                tenantA
                        )
        );
    }

    /**
     * Verifies that a role from another tenant is not
     * treated as a duplicate for the requested tenant.
     */
    @Test
    void existsByRoleName_shouldReturnFalseForDifferentTenant() {

        assertFalse(
                roleRepository
                        .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "HR Manager",
                                tenantC
                        )
        );
    }

    /**
     * Verifies that soft-deleted roles are excluded
     * from duplicate role-name checks.
     */
    @Test
    void existsByRoleName_shouldIgnoreDeletedRole() {

        assertFalse(
                roleRepository
                        .existsByRoleNameIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "Deleted Role",
                                tenantA
                        )
        );
    }

    // =========================================================
    // DUPLICATE CODE
    // =========================================================

    /**
     * Verifies that an existing role code is detected
     * within the same tenant.
     */
    @Test
    void existsByRoleCode_shouldReturnTrueForSameTenant() {

        assertTrue(
                roleRepository
                        .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "HR_MANAGER",
                                tenantA
                        )
        );
    }

    /**
     * Verifies that role-code duplicate detection
     * is case-insensitive.
     */
    @Test
    void existsByRoleCode_shouldBeCaseInsensitive() {

        assertTrue(
                roleRepository
                        .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "hr_manager",
                                tenantA
                        )
        );
    }

    /**
     * Verifies that the same role code in another tenant
     * does not count as a duplicate for tenant A.
     */
    @Test
    void existsByRoleCode_shouldReturnFalseForDifferentTenant() {

        assertFalse(
                roleRepository
                        .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "HR_MANAGER",
                                tenantB
                        )
        );
    }

    /**
     * Verifies that soft-deleted roles are excluded
     * from duplicate role-code checks.
     */
    @Test
    void existsByRoleCode_shouldIgnoreDeletedRole() {

        assertFalse(
                roleRepository
                        .existsByRoleCodeIgnoreCaseAndTenantIdAndIsDeletedFalse(
                                "DELETED_ROLE",
                                tenantA
                        )
        );
    }

    // =========================================================
    // FIND BY ID
    // =========================================================

    /**
     * Verifies that a role can be retrieved when both
     * its ID and tenant ID match.
     */
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
                                tenantA
                        );

        assertTrue(result.isPresent());

        assertEquals(
                "HR Manager",
                result.get().getRoleName()
        );
    }

    /**
     * Verifies tenant isolation by ensuring that a role
     * cannot be retrieved with another tenant ID.
     */
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
                                tenantB
                        );

        assertTrue(result.isEmpty());
    }

    // =========================================================
    // GET ALL
    // =========================================================

    /**
     * Verifies that tenant-specific retrieval returns
     * only non-deleted roles belonging to that tenant.
     */
    @Test
    void findByTenant_shouldReturnOnlyNonDeletedTenantRoles() {

        List<Role> roles =
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                tenantA
                        );

        assertEquals(3, roles.size());

        assertTrue(
                roles.stream()
                        .noneMatch(Role::getIsDeleted)
        );

        assertTrue(
                roles.stream()
                        .allMatch(r ->
                                tenantA.equals(
                                        r.getTenantId()))
        );
    }

    /**
     * Verifies that roles from another tenant are excluded.
     */
    @Test
    void findByTenant_shouldNotReturnOtherTenantRoles() {

        List<Role> roles =
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                tenantA
                        );

        assertTrue(
                roles.stream()
                        .noneMatch(r ->
                                tenantB.equals(
                                        r.getTenantId()))
        );
    }

    // =========================================================
    // PAGINATION
    // =========================================================

    /**
     * Verifies that tenant-specific role retrieval supports
     * pagination and reports the correct total element count.
     */
    @Test
    void findByTenantPaged_shouldReturnPage() {

        PageRequest pageable =
                PageRequest.of(0, 2);

        Page<Role> result =
                roleRepository
                        .findByTenantIdAndIsDeletedFalse(
                                tenantA,
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

    /**
     * Verifies that role search can match the role name.
     */
    @Test
    void searchRoles_shouldFindByRoleName() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
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

    /**
     * Verifies that role search can match the role code.
     */
    @Test
    void searchRoles_shouldFindByRoleCode() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
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

    /**
     * Verifies that role search is case-insensitive.
     */
    @Test
    void searchRoles_shouldBeCaseInsensitive() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
                        "hr manager",
                        null,
                        null
                );

        assertEquals(1, result.size());
    }

    /**
     * Verifies that search results can be filtered by role type.
     */
    @Test
    void searchRoles_shouldFilterByRoleType() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
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

    /**
     * Verifies that search results can be filtered by status.
     */
    @Test
    void searchRoles_shouldFilterByStatus() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
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

    /**
     * Verifies that all supplied search filters are applied together.
     */
    @Test
    void searchRoles_shouldApplyAllFilters() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
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

    /**
     * Verifies that search results remain isolated to
     * the requested tenant.
     */
    @Test
    void searchRoles_shouldNotReturnOtherTenantRoles() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
                        "HR",
                        null,
                        null
                );

        assertTrue(
                result.stream()
                        .allMatch(r ->
                                tenantA.equals(
                                        r.getTenantId()))
        );
    }

    /**
     * Verifies that soft-deleted roles are excluded from search.
     */
    @Test
    void searchRoles_shouldExcludeDeletedRoles() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
                        "Deleted",
                        null,
                        null
                );

        assertTrue(result.isEmpty());
    }

    /**
     * Verifies that a search without optional filters
     * returns all non-deleted roles for the tenant.
     */
    @Test
    void searchRoles_withNoFilters_shouldReturnTenantRoles() {

        List<Role> result =
                roleRepository.searchRoles(
                        tenantA,
                        null,
                        null,
                        null
                );

        assertEquals(3, result.size());
    }

    // =========================================================
    // COUNTS
    // =========================================================

    /**
     * Verifies the total number of non-deleted roles for a tenant.
     */
    @Test
    void countByTenant_shouldReturnTotalActiveRoles() {

        long count =
                roleRepository
                        .countByTenantIdAndIsDeletedFalse(
                                tenantA
                        );

        assertEquals(3, count);
    }

    /**
     * Verifies that system-role counts are tenant-specific
     * and exclude deleted roles.
     */
    @Test
    void countByTenantAndType_shouldCountSystemRoles() {

        long count =
                roleRepository
                        .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                                tenantA,
                                RoleType.SYSTEM
                        );

        assertEquals(1, count);
    }

    /**
     * Verifies that custom-role counts are tenant-specific
     * and exclude deleted roles.
     */
    @Test
    void countByTenantAndType_shouldCountCustomRoles() {

        long count =
                roleRepository
                        .countByTenantIdAndRoleTypeAndIsDeletedFalse(
                                tenantA,
                                RoleType.CUSTOM
                        );

        assertEquals(2, count);
    }

    /**
     * Verifies that soft-deleted roles are not included in counts.
     */
    @Test
    void counts_shouldNotIncludeDeletedRoles() {

        long count =
                roleRepository
                        .countByTenantIdAndIsDeletedFalse(
                                tenantA
                        );

        assertEquals(3, count);
    }

    // =========================================================
    // HELPER
    // =========================================================

    /**
     * Creates a Role entity for repository test setup.
     */
    private Role createRole(
            String roleName,
            String roleCode,
            RoleType roleType,
            String status,
            UUID tenantId,
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