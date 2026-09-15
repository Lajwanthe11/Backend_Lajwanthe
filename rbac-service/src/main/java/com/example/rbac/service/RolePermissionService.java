package com.example.rbac.service;

import com.example.common.tenant.TenantContext;
import com.example.rbac.entity.Permission;
import com.example.rbac.entity.Role;
import com.example.rbac.entity.RolePermission;
import com.example.rbac.entity.RolePermissionAudit;
import com.example.rbac.repository.PermissionRepository;
import com.example.rbac.repository.RolePermissionAuditRepository;
import com.example.rbac.repository.RolePermissionRepository;
import com.example.rbac.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RolePermissionService {

        private final RolePermissionRepository rolePermissionRepository;
        private final RolePermissionAuditRepository auditRepository;
        private final RoleRepository roleRepository;
        private final PermissionRepository permissionRepository;

        public RolePermissionService(
                        RolePermissionRepository rolePermissionRepository,
                        RolePermissionAuditRepository auditRepository,
                        RoleRepository roleRepository,
                        PermissionRepository permissionRepository) {
                this.rolePermissionRepository = rolePermissionRepository;
                this.auditRepository = auditRepository;
                this.roleRepository = roleRepository;
                this.permissionRepository = permissionRepository;
        }

        @Transactional(readOnly = true)
        public List<RolePermission> getPermissionsByRole(UUID roleId) {
                roleRepository.findById(roleId)
                                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

                return rolePermissionRepository.findByRole_IdAndActiveTrue(roleId);
        }

        @Transactional(readOnly = true)
        public RolePermission getRolePermission(
                        UUID roleId,
                        UUID permissionId) {
                return rolePermissionRepository
                                .findByRole_IdAndPermission_PermissionId(roleId, permissionId)
                                .orElse(null);
        }

        public RolePermission grantPermission(
                        UUID roleId,
                        UUID permissionId,
                        String grantedBy) {
                Role role = roleRepository.findById(roleId)
                                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

                Permission permission = permissionRepository.findById(permissionId)
                                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

                if (!permission.isActive()) {
                        throw new IllegalArgumentException("Inactive permission cannot be granted");
                }

                RolePermission rolePermission = rolePermissionRepository
                                .findByRole_IdAndPermission_PermissionId(roleId, permissionId)
                                .orElse(null);

                if (rolePermission != null && rolePermission.isActive()) {
                        return rolePermission;
                }

                if (rolePermission == null) {
                        rolePermission = new RolePermission();
                }

                rolePermission.setRole(role);
                rolePermission.setPermission(permission);
                rolePermission.setGrantedBy(grantedBy);
                rolePermission.setGrantedAt(LocalDateTime.now());
                rolePermission.setRevokedBy(null);
                rolePermission.setRevokedAt(null);
                rolePermission.setActive(true);

                RolePermission saved = rolePermissionRepository.save(rolePermission);

                saveAudit(
                                roleId,
                                permissionId,
                                grantedBy,
                                false,
                                true);

                return saved;
        }

        public void revokePermission(
                        UUID roleId,
                        UUID permissionId,
                        String revokedBy) {
                Role role = roleRepository.findById(roleId)
                                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

                Permission permission = permissionRepository.findById(permissionId)
                                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));

                if (isSuperAdmin(role) && permission.isSystem()) {
                        throw new IllegalArgumentException(
                                        "System permissions cannot be revoked from Super Admin");
                }

                RolePermission rolePermission = rolePermissionRepository
                                .findByRole_IdAndPermission_PermissionId(roleId, permissionId)
                                .orElse(null);

                if (rolePermission == null || !rolePermission.isActive()) {
                        return;
                }

                rolePermission.setActive(false);
                rolePermission.setRevokedBy(revokedBy);
                rolePermission.setRevokedAt(LocalDateTime.now());

                rolePermissionRepository.save(rolePermission);

                saveAudit(
                                roleId,
                                permissionId,
                                revokedBy,
                                true,
                                false);
        }

        private boolean isSuperAdmin(Role role) {
                if (role.getRoleCode() != null
                                && role.getRoleCode().equalsIgnoreCase("SUPER_ADMIN")) {
                        return true;
                }

                return role.getRoleName() != null
                                && role.getRoleName().equalsIgnoreCase("Super Admin");
        }

        private void saveAudit(
                        UUID roleId,
                        UUID permissionId,
                        String changedBy,
                        boolean fromGranted,
                        boolean toGranted) {
                RolePermissionAudit audit = new RolePermissionAudit();

                audit.setTenantId(TenantContext.getTenantId());
                audit.setRoleId(roleId);
                audit.setPermissionId(permissionId);
                audit.setChangedBy(changedBy);
                audit.setFromGranted(fromGranted);
                audit.setToGranted(toGranted);
                audit.setChangedAt(LocalDateTime.now());

                auditRepository.save(audit);
        }
}