package com.example.rbac.dto;

import com.example.rbac.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RoleRequestDto {
  
@NotBlank(message = "Role name is required")
private String roleName;

private String roleCode;

@NotNull(message = "Role type is required")
private RoleType roleType;

private String description;

private String status = "ACTIVE";

public String getRoleName() {
    return roleName;
}

public RoleRequestDto(String roleName, String roleCode,RoleType roleType, String description,String status) {
    this.roleName = roleName;
    this.roleCode = roleCode;
    this.roleType = roleType;
    this.description = description;
    this.status = status != null ? status : "ACTIVE";
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
}