package com.example.rbac.repository;

import com.example.rbac.entity.Permission;
import com.example.rbac.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

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
}