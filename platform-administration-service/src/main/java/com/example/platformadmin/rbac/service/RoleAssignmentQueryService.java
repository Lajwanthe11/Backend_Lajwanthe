package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.dto.response.DepartmentRoleDistribution;
import com.example.platformadmin.rbac.dto.response.ExpiringRoleAssignment;
import com.example.platformadmin.rbac.dto.response.RoleAssignmentReportRow;
import com.example.platformadmin.rbac.enums.RoleType;

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
