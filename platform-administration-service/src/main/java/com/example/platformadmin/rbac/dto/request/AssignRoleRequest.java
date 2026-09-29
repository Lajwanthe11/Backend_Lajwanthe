package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignRoleRequest {

    @NotEmpty
    @Valid
    private List<RoleAssignmentRequest> roles;
}