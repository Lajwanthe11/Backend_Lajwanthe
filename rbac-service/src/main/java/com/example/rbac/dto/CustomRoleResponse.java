package com.example.rbac.dto;

import com.example.rbac.enums.CustomRoleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CustomRoleResponse {

    //private Long roleId;
    private UUID roleId;

    private String roleName;

    private String roleCode;

    private String description;

    private CustomRoleStatus status;

    private Integer draftVersion;

    private Integer publishedVersion;

    private Integer versionNumber;

    private List<Long> permissionIds;

    private Integer permissionCount;

    private String publishNotes;

    private String publishedBy;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;


    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }


    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public CustomRoleStatus getStatus() {
        return status;
    }

    public void setStatus(CustomRoleStatus status) {
        this.status = status;
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


    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }


    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }


    public Integer getPermissionCount() {
        return permissionCount;
    }

    public void setPermissionCount(Integer permissionCount) {
        this.permissionCount = permissionCount;
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


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}