package com.example.rbac.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores the audit history of changes made to role-permission mappings.
 *
 * Each record represents one permission state change:
 * granted -> revoked
 * revoked -> granted
 *
 * This allows the system to track who changed a permission,
 * what the previous state was, what the new state is,
 * and when the change occurred.
 */
@Entity
@Table(name = "role_permission_audit")
public class RolePermissionAudit {

    // Unique identifier for this audit record.
    // UUID is used so every audit entry has its own unique ID.
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // Identifies the tenant where the permission change occurred.
    // This keeps audit information separated between tenants.
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    // Identifies the role whose permission was changed.
    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    // Identifies the permission that was changed.
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    // Identifies the user who performed the permission change.
    // This is important for security auditing and accountability.
    @Column(name = "changed_by", nullable = false)
    private UUID changedBy;

    // Stores the permission state before the change.
    // Example:
    // true -> permission was previously granted
    // false -> permission was previously not granted
    @Column(name = "from_granted", nullable = false)
    private boolean fromGranted;

    // Stores the permission state after the change.
    // Example:
    // false -> permission was revoked
    // true -> permission was granted
    @Column(name = "to_granted", nullable = false)
    private boolean toGranted;

    // Stores when the permission change happened.
    // This provides the timestamp for the audit history.
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // Required by JPA to create the entity.
    public RolePermissionAudit() {
    }

    // Automatically generates the audit record ID before inserting it.
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }

    public UUID getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(UUID changedBy) {
        this.changedBy = changedBy;
    }

    public boolean isFromGranted() {
        return fromGranted;
    }

    public void setFromGranted(boolean fromGranted) {
        this.fromGranted = fromGranted;
    }

    public boolean isToGranted() {
        return toGranted;
    }

    public void setToGranted(boolean toGranted) {
        this.toGranted = toGranted;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
