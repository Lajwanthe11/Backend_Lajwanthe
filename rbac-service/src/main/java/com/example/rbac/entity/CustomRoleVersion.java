// CustomRoleVersion.java

package com.example.rbac.entity;

import com.example.rbac.enums.CustomRoleStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_role_versions", uniqueConstraints = {@UniqueConstraint(name = "uk_custom_role_version", columnNames = {"role_id", "version_number"})})
public class CustomRoleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "role_id", nullable = false)
    private java.util.UUID roleId;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomRoleStatus status;

    @Column(name = "permission_snapshot", nullable = false, columnDefinition = "TEXT")
    private String permissionSnapshot;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "publish_notes", columnDefinition = "TEXT")
    private String publishNotes;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (createdBy == null || createdBy.isBlank()) {
            createdBy = "SYSTEM";
        }
    }

    public java.util.UUID getId() {
        return id;
    }

    public void setId(java.util.UUID id) {
        this.id = id;
    }

    public java.util.UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(java.util.UUID roleId) {
        this.roleId = roleId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public CustomRoleStatus getStatus() {
        return status;
    }

    public void setStatus(CustomRoleStatus status) {
        this.status = status;
    }

    public String getPermissionSnapshot() {
        return permissionSnapshot;
    }

    public void setPermissionSnapshot(String permissionSnapshot) {
        this.permissionSnapshot = permissionSnapshot;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(String publishedBy) {
        this.publishedBy = publishedBy;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getPublishNotes() {
        return publishNotes;
    }

    public void setPublishNotes(String publishNotes) {
        this.publishNotes = publishNotes;
    }
}