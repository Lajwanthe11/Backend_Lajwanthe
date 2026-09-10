package com.example.rbac.dto;

public record RoleTemplateSummaryDto(
        Long id,
        String name,
        String description,
        int permissionCount,
        String recommendedFor
){
}