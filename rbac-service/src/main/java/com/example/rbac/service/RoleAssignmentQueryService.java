package com.example.rbac.service;

import com.example.rbac.dto.DepartmentRoleDistribution;
import com.example.rbac.dto.ExpiringRoleAssignment;
import com.example.rbac.dto.RoleAssignmentReportRow;
import com.example.rbac.enums.RoleType;

import java.util.List;
import java.util.UUID;

public interface RoleAssignmentQueryService {
    List<RoleAssignmentReportRow> getReport(UUID tenantId);

    List<DepartmentRoleDistribution> getDistributionByDepartment(UUID tenantId);

    List<ExpiringRoleAssignment> getExpiring(
            UUID tenantId,
            int days,
            UUID organizationId,
            UUID departmentId,
            RoleType roleType
    );
}
