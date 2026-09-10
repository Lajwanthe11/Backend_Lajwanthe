package com.example.rbac.dto;

public record AccessValidationRequest(
                String userId,
                String permissionCode,
                String resourceType,
                String resourceId) {
}
