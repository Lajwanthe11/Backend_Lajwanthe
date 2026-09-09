package com.example.rbac.repository;

// CustomRoleDataRepository.java


import com.example.rbac.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomRoleDataRepository extends JpaRepository<Role, Long> {

    boolean existsByRoleNameIgnoreCaseAndTenantId(String roleName, String tenantId);

    boolean existsByRoleCodeIgnoreCaseAndTenantId(String roleCode, String tenantId);

    Optional<Role> findByIdAndTenantId(Long id, String tenantId);
}