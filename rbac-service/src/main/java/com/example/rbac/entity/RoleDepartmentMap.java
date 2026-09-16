package com.example.rbac.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

// Links a role assignment (UserRole) to a department it is scoped to
// id, tenantId, createdAt/By etc are inherited from BaseEntity
@Entity
@Table(name = "role_department_map")
public class RoleDepartmentMap extends BaseEntity {

    @Column(name = "user_role_id", nullable = false)
    private UUID userRoleId;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    public UUID getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(UUID userRoleId) {
        this.userRoleId = userRoleId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }
}