package com.example.rbac.repository;

// CustomRoleVersionRepository.java


import com.example.rbac.entity.CustomRoleVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomRoleVersionRepository extends JpaRepository<CustomRoleVersion, Long> {

    List<CustomRoleVersion> findAllByRoleIdAndTenantIdOrderByVersionNumberDesc(Long roleId, String tenantId);

    Optional<CustomRoleVersion> findByRoleIdAndTenantIdAndVersionNumber(Long roleId, String tenantId, Integer versionNumber);

    Optional<CustomRoleVersion> findTopByRoleIdAndTenantIdOrderByVersionNumberDesc(Long roleId, String tenantId);
}