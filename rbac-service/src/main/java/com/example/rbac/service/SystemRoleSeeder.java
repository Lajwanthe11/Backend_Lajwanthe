package com.example.rbac.service;

import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Seeds the 8 default system roles for a tenant. Call seedForTenant(tenantId)
 * from your tenant-provisioning workflow (e.g. a listener on a
 * TenantCreatedEvent already fired by the Tenant Management module).
 *
 * Idempotency: the (tenant_id, role_code) unique constraint on Role, combined
 * with the existsBy check below, means calling this twice for the same
 * tenant is a safe no-op on the second call — no duplicate rows, no
 * exception surfaced to the caller.
 */
@Component
public class SystemRoleSeeder {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public SystemRoleSeeder(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    private static final List<SystemRoleDefinition> SYSTEM_ROLES = List.of(
            new SystemRoleDefinition("SUPER_ADMIN", "Super Admin",
                    "Full platform access across all tenants and modules"),
            new SystemRoleDefinition("ORG_ADMIN", "Organization Admin",
                    "Full access within a single tenant/organization"),
            new SystemRoleDefinition("DEPARTMENT_MANAGER", "Department Manager",
                    "Manages a department's employees, tasks, and approvals"),
            new SystemRoleDefinition("HR_MANAGER", "HR Manager",
                    "Manages employee records, onboarding, and HR workflows"),
            new SystemRoleDefinition("FINANCE_MANAGER", "Finance Manager",
                    "Manages payroll, budgets, and financial approvals"),
            new SystemRoleDefinition("BRANCH_MANAGER", "Branch Manager",
                    "Manages a specific branch's operations and staff"),
            new SystemRoleDefinition("EMPLOYEE", "Employee",
                    "Standard employee self-service access"),
            new SystemRoleDefinition("READ_ONLY_USER", "Read-Only User",
                    "View-only access across permitted modules")
    );

    @Transactional
    public void seedForTenant(UUID tenantId) {
        for (SystemRoleDefinition def : SYSTEM_ROLES) {
            if (roleRepository.existsByTenantIdAndRoleCode(tenantId, def.code())) {
                continue; // already seeded — idempotent no-op
            }

            Role role = new Role();
            role.setTenantId(tenantId);
            role.setRoleCode(def.code());
            role.setRoleName(def.name());
            role.setDescription(def.description());
            role.setRoleType(RoleType.SYSTEM);
            role.setStatus("ACTIVE");
            role.setIsDeleted(false);
            role.setPermissions(resolvePermissions(def.code()));

            roleRepository.save(role);
        }
    }

    // Maps each system role to its default permission codes. Fill in the
    // real codes once the platform-wide Permission Matrix is finalized —
    // any code not yet in the permissions table resolves to an empty set,
    // it won't throw.
    private Set<Permission> resolvePermissions(String roleCode) {
        List<String> codes = switch (roleCode) {
            case "SUPER_ADMIN" -> List.of("*"); // wildcard — handle specially in your authorization layer
            case "ORG_ADMIN" -> List.of("ORG_MANAGE", "EMPLOYEE_MANAGE", "ROLE_READ", "ROLE_WRITE");
            case "DEPARTMENT_MANAGER" -> List.of("EMPLOYEE_VIEW", "EMPLOYEE_APPROVE", "DEPARTMENT_MANAGE");
            case "HR_MANAGER" -> List.of("EMPLOYEE_MANAGE", "ONBOARDING_MANAGE");
            case "FINANCE_MANAGER" -> List.of("PAYROLL_VIEW", "PAYROLL_APPROVE", "BUDGET_MANAGE");
            case "BRANCH_MANAGER" -> List.of("BRANCH_MANAGE", "EMPLOYEE_VIEW");
            case "EMPLOYEE" -> List.of("PROFILE_VIEW", "PROFILE_EDIT_SELF");
            case "READ_ONLY_USER" -> List.of("PROFILE_VIEW");
            default -> List.of();
        };

        return new HashSet<>(permissionRepository.findByPermissionCodeIn(codes));
    }

    private record SystemRoleDefinition(String code, String name, String description) {}}