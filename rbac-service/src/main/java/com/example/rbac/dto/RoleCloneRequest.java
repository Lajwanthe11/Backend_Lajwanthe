package com.example.rbac.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleCloneRequest(
        @NotBlank String newName
        // roleCode is always server-generated — never accepted from the client,
        // so a caller can't collide with or spoof an existing system role code.
) {}
