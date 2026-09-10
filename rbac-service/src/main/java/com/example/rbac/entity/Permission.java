package com.example.rbac.entity;

import com.example.common.abstracts.BaseEntity;
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
public class Permission extends BaseEntity {

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
}
