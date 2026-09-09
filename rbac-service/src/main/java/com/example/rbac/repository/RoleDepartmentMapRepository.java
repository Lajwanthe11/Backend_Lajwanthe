package com.example.rbac.repository;

import com.example.rbac.entity.RoleDepartmentMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleDepartmentMapRepository extends JpaRepository<RoleDepartmentMap, Long> {

    List<RoleDepartmentMap> findByUserRoleId(Long userRoleId);

    void deleteByUserRoleId(Long userRoleId);
}