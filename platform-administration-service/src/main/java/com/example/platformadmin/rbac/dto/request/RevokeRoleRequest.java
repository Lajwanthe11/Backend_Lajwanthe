package com.example.platformadmin.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevokeRoleRequest {

    @NotBlank(message = "Revoke reason is mandatory")
    private String reason;
}