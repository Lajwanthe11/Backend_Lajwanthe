package com.example.platformadmin.rbac.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Master permission registry entry.
 * Permission codes are GLOBAL (not tenant-scoped) and immutable after creation.
 * Only displayName and description may be updated post-creation.
 */
@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_permission_code", columnNames = "permission_code")
})
public class Permission {

    @Id
    @Column(name = "permission_id", updatable = false, nullable = false)
    private UUID permissionId;

    @Column(name = "permission_code", nullable = false, length = 100, updatable = false)
    private String permissionCode;

    @Column(name = "resource", nullable = false, length = 50, updatable = false)
    private String resource;

    @Column(name = "action", nullable = false, length = 50, updatable = false)
    private String action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private PermissionGroup group;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_system", nullable = false, updatable = false)
    private boolean system = true;

    @Column(name = "module", nullable = false, length = 50, updatable = false)
    private String module;

    public Permission() {
    }

    @PrePersist
    protected void onCreate() {
        // App-side UUID generation, per team decision.
        if (permissionId == null) {
            permissionId = UUID.randomUUID();
        }
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public PermissionGroup getGroup() {
        return group;
    }

    public void setGroup(PermissionGroup group) {
        this.group = group;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    // Required for correct behavior inside a HashSet (Role.permissions /
    // RoleTemplate.permissions are both Set<Permission>).
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission other = (Permission) o;
        return permissionId != null && permissionId.equals(other.permissionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
