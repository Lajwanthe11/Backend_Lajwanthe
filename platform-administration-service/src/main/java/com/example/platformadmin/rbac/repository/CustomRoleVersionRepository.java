package com.example.platformadmin.rbac.repository;

import com.example.platformadmin.rbac.entity.CustomRoleVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomRoleVersionRepository extends JpaRepository<CustomRoleVersion, UUID> {

    List<CustomRoleVersion> findAllByRoleIdAndTenantIdOrderByVersionNumberDesc(
            UUID roleId,
            String tenantId);

    Optional<CustomRoleVersion> findByRoleIdAndTenantIdAndVersionNumber(
            UUID roleId,
            String tenantId,
            Integer versionNumber);

    Optional<CustomRoleVersion> findTopByRoleIdAndTenantIdOrderByVersionNumberDesc(
            UUID roleId,
            String tenantId);
}