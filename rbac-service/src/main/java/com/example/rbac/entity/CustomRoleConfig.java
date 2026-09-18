// CustomRoleConfig.java

package com.example.rbac.entity;

import com.example.rbac.enums.CustomRoleStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_role_config", uniqueConstraints = {@UniqueConstraint(name = "uk_custom_role_config_role_tenant", columnNames = {"role_id", "tenant_id"})})
public class CustomRoleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @Column(name = "role_id", nullable = false)
    private java.util.UUID roleId;

    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;

    @Column(name = "draft_version", nullable = false)
    private Integer draftVersion = 1;

    @Column(name = "published_version", nullable = false)
    private Integer publishedVersion = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CustomRoleStatus status = CustomRoleStatus.DRAFT;

    @Column(name = "publish_notes", columnDefinition = "TEXT")
    private String publishNotes;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
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

    public Integer getDraftVersion() {
        return draftVersion;
    }

    public void setDraftVersion(Integer draftVersion) {
        this.draftVersion = draftVersion;
    }

    public Integer getPublishedVersion() {
        return publishedVersion;
    }

    public void setPublishedVersion(Integer publishedVersion) {
        this.publishedVersion = publishedVersion;
    }

    public CustomRoleStatus getStatus() {
        return status;
    }

    public void setStatus(CustomRoleStatus status) {
        this.status = status;
    }

    public String getPublishNotes() {
        return publishNotes;
    }

    public void setPublishNotes(String publishNotes) {
        this.publishNotes = publishNotes;
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
}