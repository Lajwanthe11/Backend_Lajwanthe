package com.example.platformadmin.rbac.exception;

public class PermissionDeniedException extends RuntimeException {

    private final String requestedPermission;

    public PermissionDeniedException(String message, String requestedPermission) {
        super(message);
        this.requestedPermission = requestedPermission;
    }

    /**
     * Internal only - for security_events logging. Never expose via API response.
     */
    public String getRequestedPermission() {
        return requestedPermission;
    }
}
