package com.example.rbac.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PermissionGrantRequest {

    @NotNull
    private UUID permissionId;

    @NotNull
    private Boolean granted;
}