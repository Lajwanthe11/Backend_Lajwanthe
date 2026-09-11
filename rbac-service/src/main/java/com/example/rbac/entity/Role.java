package com.example.rbac.entity;

import com.example.common.abstracts.BaseEntity;
import com.example.rbac.enums.RoleType;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private RoleType roleType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )

    // Source role this was cloned from, if any (nullable)
    @Column(name = "cloned_from_role_id")
    private Long clonedFromRoleId;

    // Template this role was created from, if any (nullable)
    @Column(name = "created_from_template_id")
    private String createdFromTemplateId;

    private Set<Permission> permissions = new HashSet<>();

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

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public String getCreatedFromTemplateId() { return createdFromTemplateId; }
    public void setCreatedFromTemplateId(String createdFromTemplateId) { this.createdFromTemplateId = createdFromTemplateId; }

    public Long getClonedFromRoleId() { return clonedFromRoleId; }
    public void setClonedFromRoleId(Long clonedFromRoleId) { this.clonedFromRoleId = clonedFromRoleId; }
}