package com.example.rbac.repository;

// CustomRoleConfigRepository.java


import com.example.rbac.entity.CustomRoleConfig;
import com.example.rbac.enums.CustomRoleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomRoleConfigRepository extends JpaRepository<CustomRoleConfig, Long> {

    Optional<CustomRoleConfig> findByRoleIdAndTenantId(Long roleId, String tenantId);

    List<CustomRoleConfig> findAllByTenantId(String tenantId);

    long countByTenantIdAndStatusNot(String tenantId, CustomRoleStatus status);

    boolean existsByRoleIdAndTenantId(Long roleId, String tenantId);
}