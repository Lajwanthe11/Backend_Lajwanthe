package com.example.rbac.entity;

import com.example.common.abstracts.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "role_department_map")
public class RoleDepartmentMap extends BaseEntity {

    @Column(name = "user_role_id", nullable = false)
    private Long userRoleId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    public Long getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(Long userRoleId) {
        this.userRoleId = userRoleId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}