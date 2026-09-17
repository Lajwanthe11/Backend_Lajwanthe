package com.example.rbac.service;

import com.example.rbac.dto.DepartmentRoleDistribution;
import com.example.rbac.dto.ExpiringRoleAssignment;
import com.example.rbac.dto.RoleAssignmentReportRow;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.UserRole;
import com.example.rbac.enums.RoleType;
import com.example.rbac.exception.RoleAssignmentValidationException;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRoleRepository;
import com.example.rbac.service.UserAssignmentSupportService;
import com.example.rbac.service.serviceImpl.RoleAssignmentQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentQueryServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserAssignmentSupportService userAssignmentSupportService;

    private RoleAssignmentQueryServiceImpl service;
    private UUID tenantId;
    private UUID roleId;
    private Role role;

    @BeforeEach
    void setUp() {
        service = new RoleAssignmentQueryServiceImpl(
                userRoleRepository, roleRepository, userAssignmentSupportService);
        tenantId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        role = new Role();
        role.setId(roleId);
        role.setTenantId(tenantId);
        role.setRoleCode("HR_MANAGER");
        role.setRoleName("HR Manager");
        role.setRoleType(RoleType.SYSTEM);
        role.isActive(true);
    }

    @Test
    void getReport_shouldCalculateActiveScheduledExpiredAndRevokedStatuses() {
        LocalDate today = LocalDate.now();

        UserRole active = assignment(UUID.randomUUID(), today.minusDays(1), today.plusDays(10), true);
        UserRole scheduled = assignment(UUID.randomUUID(), today.plusDays(2), today.plusDays(20), true);
        UserRole expired = assignment(UUID.randomUUID(), today.minusDays(20), today.minusDays(1), true);
        UserRole revoked = assignment(UUID.randomUUID(), today.minusDays(2), today.plusDays(20), false);

        when(userRoleRepository.findAllByTenantId(tenantId))
                .thenReturn(List.of(active, scheduled, expired, revoked));
        when(roleRepository.findAllById(any())).thenReturn(List.of(role));

        List<RoleAssignmentReportRow> rows = service.getReport(tenantId);
        Map<UUID, String> statuses = rows.stream()
                .collect(Collectors.toMap(RoleAssignmentReportRow::userId, RoleAssignmentReportRow::status));

        assertEquals("ACTIVE", statuses.get(active.getUserId()));
        assertEquals("SCHEDULED", statuses.get(scheduled.getUserId()));
        assertEquals("EXPIRED", statuses.get(expired.getUserId()));
        assertEquals("REVOKED", statuses.get(revoked.getUserId()));
        assertTrue(rows.stream().allMatch(row -> "HR_MANAGER".equals(row.roleCode())));
    }

    @Test
    void getDistributionByDepartment_shouldGroupUsersByDepartmentAndRole() {
        UUID departmentId = UUID.randomUUID();
        UserRole first = assignment(UUID.randomUUID(), LocalDate.now(), null, true);
        UserRole second = assignment(UUID.randomUUID(), LocalDate.now(), null, true);

        when(userRoleRepository.findCurrentActiveAssignments(eq(tenantId), any(LocalDate.class)))
                .thenReturn(List.of(first, second));
        when(roleRepository.findAllById(any())).thenReturn(List.of(role));
        when(userAssignmentSupportService.resolveDepartmentId(tenantId, first.getUserId()))
                .thenReturn(Optional.of(departmentId));
        when(userAssignmentSupportService.resolveDepartmentId(tenantId, second.getUserId()))
                .thenReturn(Optional.of(departmentId));

        List<DepartmentRoleDistribution> result = service.getDistributionByDepartment(tenantId);

        assertEquals(1, result.size());
        DepartmentRoleDistribution row = result.get(0);
        assertEquals(departmentId, row.departmentId());
        assertEquals(roleId, row.roleId());
        assertEquals("HR_MANAGER", row.roleCode());
        assertEquals(2L, row.userCount());
    }

    @Test
    void getDistributionByDepartment_shouldAllowUnresolvedDepartment() {
        UserRole assignment = assignment(UUID.randomUUID(), LocalDate.now(), null, true);

        when(userRoleRepository.findCurrentActiveAssignments(eq(tenantId), any(LocalDate.class)))
                .thenReturn(List.of(assignment));
        when(roleRepository.findAllById(any())).thenReturn(List.of(role));
        when(userAssignmentSupportService.resolveDepartmentId(tenantId, assignment.getUserId()))
                .thenReturn(Optional.empty());

        List<DepartmentRoleDistribution> result = service.getDistributionByDepartment(tenantId);

        assertEquals(1, result.size());
        assertNull(result.get(0).departmentId());
    }

    @Test
    void getExpiring_shouldReturnFilteredAssignmentsAndDaysToExpiry() {
        LocalDate today = LocalDate.now();
        UUID departmentId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UserRole assignment = assignment(
                UUID.randomUUID(), today.minusDays(2), today.plusDays(7), true);

        when(userRoleRepository.findExpiringAssignments(
                eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(assignment));
        when(roleRepository.findAllById(any())).thenReturn(List.of(role));
        when(userAssignmentSupportService.resolveDepartmentId(tenantId, assignment.getUserId()))
                .thenReturn(Optional.of(departmentId));
        when(userAssignmentSupportService.resolveOrganizationId(tenantId, assignment.getUserId()))
                .thenReturn(Optional.of(organizationId));

        List<ExpiringRoleAssignment> result = service.getExpiring(
                tenantId, 7, organizationId, departmentId, RoleType.SYSTEM);

        assertEquals(1, result.size());
        ExpiringRoleAssignment row = result.get(0);
        assertEquals(7L, row.daysToExpiry());
        assertEquals("HR_MANAGER", row.roleCode());
        assertEquals(RoleType.SYSTEM, row.roleType());
    }

    @Test
    void getExpiring_shouldFilterOutDifferentRoleType() {
        UserRole assignment = assignment(
                UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(7), true);

        when(userRoleRepository.findExpiringAssignments(
                eq(tenantId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(assignment));
        when(roleRepository.findAllById(any())).thenReturn(List.of(role));

        List<ExpiringRoleAssignment> result = service.getExpiring(
                tenantId, 7, null, null, RoleType.CUSTOM);

        assertTrue(result.isEmpty());
    }

    @Test
    void getExpiring_shouldRejectUnsupportedWindow() {
        RoleAssignmentValidationException ex = assertThrows(
                RoleAssignmentValidationException.class,
                () -> service.getExpiring(tenantId, 10, null, null, null));

        assertEquals("days must be one of 7, 14, or 30", ex.getMessage());
        verifyNoInteractions(userRoleRepository);
    }

    private UserRole assignment(UUID userId, LocalDate effectiveDate, LocalDate expiryDate, boolean active) {
        return UserRole.builder()
                .userRoleId(UUID.randomUUID())
                .tenantId(tenantId)
                .userId(userId)
                .roleId(roleId)
                .primary(false)
                .effectiveDate(effectiveDate)
                .expiryDate(expiryDate)
                .assignedBy(UUID.randomUUID())
                .assignedAt(LocalDateTime.now())
                .active(active)
                .build();
    }
}
