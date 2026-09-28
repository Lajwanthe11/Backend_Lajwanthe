package com.example.rbac.repository;

import com.example.rbac.entity.Role;
import com.example.rbac.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomRoleDataRepository extends JpaRepository<Role, UUID> {

    @Query("""
        SELECT COUNT(r) > 0
        FROM Role r
        WHERE LOWER(r.roleName) = LOWER(:roleName)
          AND CAST(r.tenantId AS string) = :tenantId
          AND r.roleType = :roleType
          AND r.isDeleted = false
        """)
    boolean existsByRoleNameIgnoreCaseAndTenantId(
            @Param("roleName") String roleName,
            @Param("tenantId") String tenantId,
            @Param("roleType") RoleType roleType
    );

    @Query("""
        SELECT COUNT(r) > 0
        FROM Role r
        WHERE LOWER(r.roleCode) = LOWER(:roleCode)
          AND CAST(r.tenantId AS string) = :tenantId
          AND r.roleType = :roleType
          AND r.isDeleted = false
        """)
    boolean existsByRoleCodeIgnoreCaseAndTenantId(
            @Param("roleCode") String roleCode,
            @Param("tenantId") String tenantId,
            @Param("roleType") RoleType roleType
    );

    @Query("""
        SELECT r
        FROM Role r
        WHERE r.id = :id
          AND CAST(r.tenantId AS string) = :tenantId
          AND r.roleType = :roleType
        """)
    Optional<Role> findByIdAndTenantIdAndRoleType(
            @Param("id") UUID id,
            @Param("tenantId") String tenantId,
            @Param("roleType") RoleType roleType
    );
}