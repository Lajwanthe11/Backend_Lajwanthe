package com.example.rbac.repository;

import com.example.rbac.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomRoleDataRepository extends JpaRepository<Role, UUID> {

    boolean existsByRoleNameIgnoreCaseAndTenantId(
            String roleName,
            String tenantId);

    boolean existsByRoleCodeIgnoreCaseAndTenantId(
            String roleCode,
            String tenantId);

    Optional<Role> findByIdAndTenantId(
            UUID id,
            String tenantId);
}