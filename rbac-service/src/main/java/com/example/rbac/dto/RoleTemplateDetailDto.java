package com.example.rbac.dto;


public record RoleTemplateDetailDto(
        String id,
        String name,
        String description,
        String recommendedFor
) {}