package com.example.rbac.repository;

import com.example.rbac.entity.RoleDepartmentMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleDepartmentMapRepository extends JpaRepository<RoleDepartmentMap, Long> {

    List<RoleDepartmentMap> findByUserRoleId(UUID userRoleId);

    void deleteByUserRoleId(UUID userRoleId);

    List<RoleDepartmentMap> findByDepartmentId(UUID departmentId);
}