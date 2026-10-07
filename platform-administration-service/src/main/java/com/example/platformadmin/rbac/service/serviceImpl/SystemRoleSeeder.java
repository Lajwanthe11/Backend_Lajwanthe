package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.platformadmin.rbac.entity.Permission;
import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.enums.RoleType;
import com.example.platformadmin.rbac.repository.PermissionRepository;
import com.example.platformadmin.rbac.repository.RoleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class SystemRoleSeeder {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // Constructor to inject role and permission repositories
    public SystemRoleSeeder(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    // Defines the system roles created for each tenant
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

    // Creates the default system roles for a tenant
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

    // Finds the permissions assigned to a specific system role
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

    // Stores the code, name, and description of a system role
    private record SystemRoleDefinition(String code, String name, String description) {}
}