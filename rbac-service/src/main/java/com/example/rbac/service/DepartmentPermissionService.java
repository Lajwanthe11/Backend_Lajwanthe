package com.example.rbac.service;

import com.example.rbac.dto.DeptScopeReportDto;

import java.util.List;
import java.util.UUID;

public interface DepartmentPermissionService {

    List<UUID> getDepartmentScope(UUID userRoleId);

    void updateDepartmentScope(UUID userRoleId, List<UUID> departmentIds);

    void removeAllDepartmentScope(UUID userRoleId);

    List<DeptScopeReportDto> getDeptScopeReport();

    List<UUID> getUsersByPermission(UUID departmentId, String permissionCode);
}