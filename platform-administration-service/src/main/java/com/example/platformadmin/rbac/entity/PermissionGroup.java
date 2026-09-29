package com.example.platformadmin.rbac.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Logical grouping of permissions (e.g. "User Management Permissions").
 * Rendered as a collapsible section in the Permission Matrix UI (Varsha's Part 6).
 */
@Entity
@Table(name = "permission_groups", uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_code", columnNames = "group_code")
})
public class PermissionGroup {

    @Id
    @Column(name = "group_id", updatable = false, nullable = false)
    private UUID groupId;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "group_code", nullable = false, length = 50, updatable = false)
    private String groupCode;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public PermissionGroup() {
    }

    @PrePersist
    protected void onCreate() {
        if (groupId == null) {
            groupId = UUID.randomUUID();
        }
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
