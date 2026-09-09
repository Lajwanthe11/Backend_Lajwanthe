package com.example.rbac.service;

import java.util.List;

public interface DepartmentPermissionService {

    List<Long> getDepartmentScope(Long userRoleId);

    void updateDepartmentScope(Long userRoleId, List<Long> departmentIds);

    void removeAllDepartmentScope(Long userRoleId);
}