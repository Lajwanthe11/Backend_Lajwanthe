package com.example.rbac.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "role_permission_audit")
public class RolePermissionAudit {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "from_granted", nullable = false)
    private boolean fromGranted;

    @Column(name = "to_granted", nullable = false)
    private boolean toGranted;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public RolePermissionAudit() {
    }

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

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
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

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
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