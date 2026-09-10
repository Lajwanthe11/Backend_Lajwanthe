package com.example.rbac.dto;


import java.util.Set;

public record RoleTemplateDetailDto(
        Long id,
        String name,
        String description,
        Set<String> permissionCodes,
        String recommendedFor
) {
}