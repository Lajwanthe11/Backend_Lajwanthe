package com.example.rbac.repository;

import com.example.rbac.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolePermissionRepository
        extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole_IdAndActiveTrue(Long roleId);

    List<RolePermission> findByRole_Id(Long roleId);

    List<RolePermission> findByRole_IdInAndActiveTrue(
            List<Long> roleIds
    );

    Optional<RolePermission> findByRole_IdAndPermission_PermissionId(
            Long roleId,
            UUID permissionId
    );
}