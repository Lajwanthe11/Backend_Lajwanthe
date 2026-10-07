package com.example.platformadmin.rbac.repository;

import com.example.platformadmin.rbac.entity.RoleDepartmentMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleDepartmentMapRepository extends JpaRepository<RoleDepartmentMap, Long> {

    // All department scope rows for a given role assignment
    List<RoleDepartmentMap> findByUserRoleId(UUID userRoleId);

    // Used when resetting/removing scope for a role assignment
    void deleteByUserRoleId(UUID userRoleId);

    // All role assignments scoped to a given department
    List<RoleDepartmentMap> findByDepartmentId(UUID departmentId);
}