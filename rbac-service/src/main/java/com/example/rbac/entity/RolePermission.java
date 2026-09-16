package com.example.rbac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps a role to a permission.
 *
 * This table is the main RBAC mapping used by the Permission Matrix.
 * Each record represents whether a particular role has a particular permission
 * within a tenant.
 */
@Entity
@Table(name = "role_permissions", uniqueConstraints = {
                // Prevents duplicate role-permission mappings
                // for the same tenant.
                @UniqueConstraint(name = "uk_role_permission", columnNames = {
                                "role_id",
                                "permission_id",
                                "tenant_id"
                })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

        // Primary key for the role_permissions table.
        // UUID is used so the mapping record has its own unique identifier.
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(name = "id", nullable = false, updatable = false)
        private UUID id;

        // Identifies which role receives the permission.
        // Many RolePermission records can belong to one Role.
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "role_id", nullable = false)
        private Role role;

        // Identifies which permission is assigned to the role.
        // Many RolePermission records can reference one Permission.
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "permission_id", nullable = false)
        private Permission permission;

        // Identifies the tenant to which this role-permission mapping belongs.
        // This prevents permissions from one tenant being applied to another tenant.
        @Column(name = "tenant_id", nullable = false)
        private UUID tenantId;

        // Stores the user who originally granted the permission.
        // Useful for auditing who made the permission assignment.
        @Column(name = "granted_by", nullable = false)
        private UUID grantedBy;

        // Stores the date and time when the permission was granted.
        // Used for auditing and tracking permission changes.
        @Column(name = "granted_at", nullable = false)
        private LocalDateTime grantedAt;

        // Stores the user who revoked the permission, if it was revoked.
        // It remains null while the permission is active.
        @Column(name = "revoked_by")
        private UUID revokedBy;

        // Stores the date and time when the permission was revoked.
        // It remains null while the permission is active.
        @Column(name = "revoked_at")
        private LocalDateTime revokedAt;

        // Indicates whether the role currently has this permission.
        // true = permission is currently granted.
        // false = permission has been revoked/deactivated.
        // Keeping the record instead of deleting it allows the system
        // to preserve the permission history and audit information.
        @Column(name = "is_active", nullable = false)
        private boolean active = true;
}
