package com.example.platformadmin.rbac.dto.request;

import com.example.platformadmin.rbac.entity.Role;

import jakarta.validation.constraints.NotBlank;

public class RoleCloneRequest {

    @NotBlank(message = "New role name is required")
    private String newName;

    public RoleCloneRequest(String newName) {
        this.newName = newName;
    }

    public String getNewName() { return newName; }
}