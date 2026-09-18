package com.example.rbac.dto;

// Request payload for validating whether a user has a specific permission on a resource.
public record AccessValidationRequest(
                String userId,
                String permissionCode,
                String resourceType,
                String resourceId) {
}
