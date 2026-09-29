package com.example.platformadmin.rbac.dto.response;

// A tiny record containing the authenticated userId and tenantId( Think: “Who is this person and which tenant are they in?”)
public record AuthenticatedUser(String userId, String tenantId) {
}
