package com.example.platformadmin.rbac.service.serviceImpl;

import com.example.common.tenant.TenantContext;
import com.example.platformadmin.rbac.entity.Permission;
import com.example.platformadmin.rbac.entity.Role;
import com.example.platformadmin.rbac.entity.RolePermission;
import com.example.platformadmin.rbac.entity.RolePermissionAudit;
import com.example.platformadmin.rbac.repository.PermissionRepository;
import com.example.platformadmin.rbac.repository.RolePermissionAuditRepository;
import com.example.platformadmin.rbac.repository.RolePermissionRepository;
import com.example.platformadmin.rbac.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for granting, revoking and retrieving
 * role-permission mappings.
 *
 * This service contains the main business logic used by the
 * Permission Matrix and role permission APIs.
 */
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

        /**
         * Returns all active permissions assigned to a role.
         *
         * The role is checked first so that a missing role is reported
         * instead of returning an empty permission list.
         */
        @Transactional(readOnly = true)
        public List<RolePermission> getPermissionsByRole(UUID roleId) {

                roleRepository.findById(roleId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException("Role not found"));

                return rolePermissionRepository
                                .findByRole_IdAndActiveTrue(roleId);
        }

        /**
         * Returns the role-permission mapping for a specific role
         * and permission.
         *
         * Returns null when no mapping exists.
         */
        @Transactional(readOnly = true)
        public RolePermission getRolePermission(
                        UUID roleId,
                        UUID permissionId) {

                return rolePermissionRepository
                                .findByRole_IdAndPermission_PermissionId(
                                                roleId,
                                                permissionId)
                                .orElse(null);
        }

        /**
         * Grants a permission to a role.
         *
         * If the mapping already exists and is active, the existing
         * mapping is returned without creating another record.
         *
         * If a previously revoked mapping exists, it is reactivated.
         */
        public RolePermission grantPermission(
                        UUID roleId,
                        UUID permissionId,
                        UUID grantedBy) {

                // Verify that the role exists.
                Role role = roleRepository.findById(roleId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException("Role not found"));

                // Verify that the permission exists.
                Permission permission = permissionRepository.findById(permissionId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException("Permission not found"));

                // Inactive permissions cannot be granted.
                if (!permission.isActive()) {
                        throw new IllegalArgumentException(
                                        "Inactive permission cannot be granted");
                }

                // Look for an existing role-permission mapping.
                RolePermission rolePermission = rolePermissionRepository
                                .findByRole_IdAndPermission_PermissionId(
                                                roleId,
                                                permissionId)
                                .orElse(null);

                // If it is already active, no new update is required.
                if (rolePermission != null && rolePermission.isActive()) {
                        return rolePermission;
                }

                // Create a new mapping when no previous mapping exists.
                if (rolePermission == null) {
                        rolePermission = new RolePermission();
                }

                // Set the role and permission being mapped.
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);

                // Store the tenant associated with this mapping.
                rolePermission.setTenantId(getTenantId().toString());

                // Store who granted the permission and when.
                rolePermission.setGrantedBy(grantedBy != null ? grantedBy.toString() : null);
                rolePermission.setGrantedAt(LocalDateTime.now());

                // Clear previous revoke information when reactivating.
                rolePermission.setRevokedBy(null);
                rolePermission.setRevokedAt(null);

                // Mark the permission as currently active.
                rolePermission.setActive(true);

                RolePermission saved = rolePermissionRepository.save(rolePermission);

                // Record the permission state change in the audit table.
                saveAudit(
                                roleId,
                                permissionId,
                                grantedBy,
                                false,
                                true);

                return saved;
        }

        /**
         * Revokes a permission from a role.
         *
         * The mapping is not deleted. Instead, it is marked inactive
         * so that the permission history can be retained.
         */
        public void revokePermission(
                        UUID roleId,
                        UUID permissionId,
                        UUID revokedBy) {

                // Verify that the role exists.
                Role role = roleRepository.findById(roleId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException("Role not found"));

                // Verify that the permission exists.
                Permission permission = permissionRepository.findById(permissionId)
                                .orElseThrow(
                                                () -> new IllegalArgumentException("Permission not found"));

                // System permissions cannot be removed from Super Admin.
                if (isSuperAdmin(role) && permission.isSystem()) {
                        throw new IllegalArgumentException(
                                        "System permissions cannot be revoked from Super Admin");
                }

                // Find the existing role-permission mapping.
                RolePermission rolePermission = rolePermissionRepository
                                .findByRole_IdAndPermission_PermissionId(
                                                roleId,
                                                permissionId)
                                .orElse(null);

                // Nothing needs to be changed when the mapping does not
                // exist or has already been revoked.
                if (rolePermission == null || !rolePermission.isActive()) {
                        return;
                }

                // Mark the mapping as revoked.
                rolePermission.setActive(false);
                rolePermission.setRevokedBy(revokedBy != null ? revokedBy.toString() : null);
                rolePermission.setRevokedAt(LocalDateTime.now());

                rolePermissionRepository.save(rolePermission);

                // Record the revoke operation in the audit table.
                saveAudit(
                                roleId,
                                permissionId,
                                revokedBy,
                                true,
                                false);
        }

        /**
         * Checks whether the supplied role is the Super Admin role.
         *
         * The role code is checked first. The role name is used as
         * a fallback.
         */
        private boolean isSuperAdmin(Role role) {

                if (role.getRoleCode() != null
                                && role.getRoleCode().equalsIgnoreCase("SUPER_ADMIN")) {
                        return true;
                }

                return role.getRoleName() != null
                                && role.getRoleName().equalsIgnoreCase("Super Admin");
        }

        /**
         * Stores an audit record whenever a permission changes state.
         *
         * fromGranted = previous permission state.
         * toGranted = new permission state.
         */
        private void saveAudit(
                        UUID roleId,
                        UUID permissionId,
                        UUID changedBy,
                        boolean fromGranted,
                        boolean toGranted) {

                RolePermissionAudit audit = new RolePermissionAudit();

                // Store the tenant for audit isolation.
                audit.setTenantId(getTenantId().toString());

                // Store which role and permission changed.
                audit.setRoleId(roleId);
                audit.setPermissionId(permissionId);

                // Store who made the change.
                audit.setChangedBy(changedBy != null ? changedBy.toString() : null);

                // Store the previous and new states.
                audit.setFromGranted(fromGranted);
                audit.setToGranted(toGranted);

                // Store when the change occurred.
                audit.setChangedAt(LocalDateTime.now());

                auditRepository.save(audit);
        }

        /**
         * Reads the tenant ID from TenantContext and converts it
         * to UUID because the role-permission tables use UUID
         * tenant identifiers.
         */
        private UUID getTenantId() {

                String tenantId = TenantContext.getTenantId();

                if (tenantId == null || tenantId.isBlank()) {
                        throw new IllegalStateException(
                                        "Tenant ID is not available");
                }

                try {
                        return UUID.fromString(tenantId);
                } catch (IllegalArgumentException ex) {
                        if ("default".equalsIgnoreCase(tenantId)) {
                                return new UUID(0L, 0L);
                        }
                        throw new IllegalStateException(
                                        "Tenant ID must be a valid UUID: " + tenantId);
                }
        }
}
