package com.example.rbac.dto;

import jakarta.validation.constraints.NotBlank;

public class RoleCloneRequest {

    @NotBlank(message = "New role name is required")
    private String newName;

    public RoleCloneRequest() {
    }

    public RoleCloneRequest(String newName) {
        this.newName = newName;
    }

    public String getNewName() { return newName; }
    public void setNewName(String newName) { this.newName = newName; }
}