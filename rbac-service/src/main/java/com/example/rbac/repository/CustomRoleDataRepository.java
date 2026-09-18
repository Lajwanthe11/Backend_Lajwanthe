package com.example.rbac.repository;

import com.example.rbac.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomRoleDataRepository extends JpaRepository<Role, UUID> {

    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE LOWER(r.roleName) = LOWER(:roleName) AND CAST(r.tenantId AS string) = :tenantId")
    boolean existsByRoleNameIgnoreCaseAndTenantId(
            @Param("roleName") String roleName,
            @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(r) > 0 FROM Role r WHERE LOWER(r.roleCode) = LOWER(:roleCode) AND CAST(r.tenantId AS string) = :tenantId")
    boolean existsByRoleCodeIgnoreCaseAndTenantId(
            @Param("roleCode") String roleCode,
            @Param("tenantId") String tenantId);

    @Query("SELECT r FROM Role r WHERE r.id = :id AND CAST(r.tenantId AS string) = :tenantId")
    Optional<Role> findByIdAndTenantId(
            @Param("id") UUID id,
            @Param("tenantId") String tenantId);
}