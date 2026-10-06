package com.example.platformadmin.rbac.dto.response;

import com.example.platformadmin.rbac.entity.Permission;

// DTO returned to the client when access is denied (HTTP 403).
public record AccessDeniedResponse(int status, String error, String message) {

    // Returns a standard 403 FORBIDDEN response with a default error message.
    public static AccessDeniedResponse standard() {
        return new AccessDeniedResponse(
                403, "FORBIDDEN", "You do not have permission to perform this action");
    }
}
