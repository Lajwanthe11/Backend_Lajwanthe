package com.example.platformadmin.rbac.repository;

import com.example.platformadmin.rbac.entity.RolePermissionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RolePermissionAuditRepository
                extends JpaRepository<RolePermissionAudit, UUID> {

        List<RolePermissionAudit> findByRoleIdOrderByChangedAtDesc(UUID roleId);

        List<RolePermissionAudit> findByPermissionIdOrderByChangedAtDesc(
                        UUID permissionId);
}