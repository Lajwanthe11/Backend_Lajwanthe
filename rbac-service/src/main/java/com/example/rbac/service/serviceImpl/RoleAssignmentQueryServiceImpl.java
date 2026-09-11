package com.example.rbac.service.impl;

import com.example.rbac.service.UserAssignmentSupportService;
import com.example.rbac.dto.DepartmentRoleDistribution;
import com.example.rbac.dto.ExpiringRoleAssignment;
import com.example.rbac.dto.RoleAssignmentReportRow;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.UserRole;
import com.example.rbac.enums.RoleType;
import com.example.rbac.exception.RoleAssignmentValidationException;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRoleRepository;
import com.example.rbac.service.RoleAssignmentQueryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoleAssignmentQueryServiceImpl implements RoleAssignmentQueryService {

    private static final Set<Integer> ALLOWED_EXPIRY_WINDOWS = Set.of(7, 14, 30);

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserAssignmentSupportService userAssignmentSupportService;

    public RoleAssignmentQueryServiceImpl(
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            UserAssignmentSupportService userAssignmentSupportService
    ) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.userAssignmentSupportService = userAssignmentSupportService;
    }

    @Override
    public List<RoleAssignmentReportRow> getReport(UUID tenantId) {
        List<UserRole> assignments = userRoleRepository.findAllByTenantId(tenantId);
        Map<UUID, Role> roles = loadRoles(assignments);
        LocalDate today = LocalDate.now();

        return assignments.stream()
                .sorted(Comparator.comparing(
                        UserRole::getAssignedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(assignment -> toReportRow(
                        assignment, roles.get(assignment.getRoleId()), today))
                .toList();
    }

    @Override
    public List<DepartmentRoleDistribution> getDistributionByDepartment(UUID tenantId) {
        List<UserRole> assignments =
                userRoleRepository.findCurrentActiveAssignments(tenantId, LocalDate.now());
        Map<UUID, Role> roles = loadRoles(assignments);

        record Key(UUID departmentId, UUID roleId) {}

        Map<Key, Long> counts = assignments.stream().collect(Collectors.groupingBy(
                assignment -> new Key(
                        userAssignmentSupportService.resolveDepartmentId(tenantId, assignment.getUserId())
                                .orElse(null),
                        assignment.getRoleId()
                ),
                LinkedHashMap::new,
                Collectors.counting()
        ));

        List<DepartmentRoleDistribution> result = new ArrayList<>();
        for (Map.Entry<Key, Long> entry : counts.entrySet()) {
            Role role = roles.get(entry.getKey().roleId());
            result.add(new DepartmentRoleDistribution(
                    entry.getKey().departmentId(),
                    entry.getKey().roleId(),
                    role == null ? null : role.getRoleCode(),
                    role == null ? null : role.getRoleName(),
                    entry.getValue()
            ));
        }

        result.sort(Comparator
                .comparing(
                        DepartmentRoleDistribution::departmentId,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(item -> item.roleCode() == null ? "" : item.roleCode()));
        return List.copyOf(result);
    }

    @Override
    public List<ExpiringRoleAssignment> getExpiring(
            UUID tenantId,
            int days,
            UUID organizationId,
            UUID departmentId,
            RoleType roleType
    ) {
        if (!ALLOWED_EXPIRY_WINDOWS.contains(days)) {
            throw new RoleAssignmentValidationException("days must be one of 7, 14, or 30");
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        List<UserRole> assignments =
                userRoleRepository.findExpiringAssignments(tenantId, today, endDate);
        Map<UUID, Role> roles = loadRoles(assignments);

        return assignments.stream()
                .filter(assignment -> matchesRoleType(assignment, roles, roleType))
                .filter(assignment -> matchesDepartment(tenantId, assignment, departmentId))
                .filter(assignment -> matchesOrganization(tenantId, assignment, organizationId))
                .sorted(Comparator.comparing(UserRole::getExpiryDate))
                .map(assignment -> {
                    Role role = roles.get(assignment.getRoleId());
                    return new ExpiringRoleAssignment(
                            assignment.getUserRoleId(),
                            assignment.getUserId(),
                            assignment.getRoleId(),
                            role == null ? null : role.getRoleCode(),
                            role == null ? null : role.getRoleName(),
                            role == null ? null : role.getRoleType(),
                            assignment.getEffectiveDate(),
                            assignment.getExpiryDate(),
                            ChronoUnit.DAYS.between(today, assignment.getExpiryDate())
                    );
                })
                .toList();
    }

    private boolean matchesRoleType(
            UserRole assignment,
            Map<UUID, Role> roles,
            RoleType roleType
    ) {
        if (roleType == null) {
            return true;
        }
        Role role = roles.get(assignment.getRoleId());
        return role != null && roleType == role.getRoleType();
    }

    private boolean matchesDepartment(
            UUID tenantId,
            UserRole assignment,
            UUID departmentId
    ) {
        if (departmentId == null) {
            return true;
        }
        return userAssignmentSupportService.resolveDepartmentId(tenantId, assignment.getUserId())
                .map(departmentId::equals)
                .orElse(false);
    }

    private boolean matchesOrganization(
            UUID tenantId,
            UserRole assignment,
            UUID organizationId
    ) {
        if (organizationId == null) {
            return true;
        }
        return userAssignmentSupportService.resolveOrganizationId(tenantId, assignment.getUserId())
                .map(organizationId::equals)
                .orElse(false);
    }

    private Map<UUID, Role> loadRoles(List<UserRole> assignments) {
        Set<UUID> roleIds = assignments.stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Role> roles = new HashMap<>();
        roleRepository.findAllById(roleIds)
                .forEach(role -> roles.put(role.getRoleId(), role));
        return roles;
    }

    private RoleAssignmentReportRow toReportRow(
            UserRole assignment,
            Role role,
            LocalDate today
    ) {
        return new RoleAssignmentReportRow(
                assignment.getUserRoleId(),
                assignment.getUserId(),
                assignment.getRoleId(),
                role == null ? null : role.getRoleCode(),
                role == null ? null : role.getRoleName(),
                assignment.getEffectiveDate(),
                assignment.getExpiryDate(),
                statusOf(assignment, today),
                assignment.isPrimary(),
                assignment.getAssignedAt()
        );
    }

    private String statusOf(UserRole assignment, LocalDate today) {
        if (!assignment.isActive()) {
            return "REVOKED";
        }
        if (assignment.getEffectiveDate().isAfter(today)) {
            return "SCHEDULED";
        }
        if (assignment.getExpiryDate() != null
                && assignment.getExpiryDate().isBefore(today)) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}
