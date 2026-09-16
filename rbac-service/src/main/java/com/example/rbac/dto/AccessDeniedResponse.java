package com.example.rbac.dto;

public record AccessDeniedResponse(int status, String error, String message) {

    public static AccessDeniedResponse standard() {
        return new AccessDeniedResponse(
                403, "FORBIDDEN", "You do not have permission to perform this action");
    }
}
