package com.example.rbac.dto;

// A tiny record containing the authenticated userId and tenantId( Think: “Who is this person and which tenant are they in?”)
public record AuthenticatedUser(String userId, String tenantId) {
}
