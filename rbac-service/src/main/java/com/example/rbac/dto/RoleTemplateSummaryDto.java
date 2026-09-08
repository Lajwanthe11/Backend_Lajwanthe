package com.example.rbac.dto;

public record RoleTemplateSummaryDto(
        String id,
        String name,
        String description,
        String recommendedFor
){
}