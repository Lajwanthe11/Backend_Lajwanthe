package com.example.platformadmin.rbac.repository;

import com.example.platformadmin.rbac.entity.CustomRoleConfig;
import com.example.platformadmin.rbac.enums.CustomRoleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomRoleConfigRepository extends JpaRepository<CustomRoleConfig, Long> {

    Optional<CustomRoleConfig> findByRoleIdAndTenantId(
            UUID roleId,
            String tenantId);

    List<CustomRoleConfig> findAllByTenantId(String tenantId);

    long countByTenantIdAndStatusNot(
            String tenantId,
            CustomRoleStatus status);

    boolean existsByRoleIdAndTenantId(
            UUID roleId,
            String tenantId);
}