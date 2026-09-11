package com.example.rbac.service;

import com.example.rbac.entity.UserRole;

/**
 * Handles the Part 4 expiry-notification action.
 * The default implementation logs the notification until the shared notification service is merged.
 */
public interface RoleExpiryNotificationService {

    void notifyUserAndAdmin(UserRole assignment, String roleCode, String roleName);
}
