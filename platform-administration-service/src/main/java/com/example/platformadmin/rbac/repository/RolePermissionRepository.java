package com.example.platformadmin.rbac.repository;

import com.example.platformadmin.rbac.entity.RolePermission;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolePermissionRepository
                extends JpaRepository<RolePermission, UUID> {

        List<RolePermission> findByRole_IdAndActiveTrue(UUID roleId);

        List<RolePermission> findByRole_Id(UUID roleId);

        List<RolePermission> findByRole_IdInAndActiveTrue(
                        List<UUID> roleIds);

        Optional<RolePermission> findByRole_IdAndPermission_PermissionId(
                        UUID roleId,
                        UUID permissionId);
        @Modifying
        @Query(value = """
    INSERT INTO role_permissions (role_id,permission_id,tenant_id,granted_by,granted_at,is_active,created_at,updated_at)
        VALUES (:roleId,:permissionId,:tenantId,:grantedBy,CURRENT_TIMESTAMP,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)
            """, nativeQuery = true)
        void insertRolePermission(
                @Param("roleId") UUID roleId,
                @Param("permissionId") UUID permissionId,
                @Param("tenantId") UUID tenantId,
                @Param("grantedBy") String grantedBy
        );
}