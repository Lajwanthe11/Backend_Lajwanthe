package com.example.platformadmin.rbac.service;

import com.example.platformadmin.rbac.dto.response.ExpiryNotificationResponse;

import java.util.UUID;

public interface RoleExpiryService {
    ExpiryNotificationResponse triggerForTenant(UUID tenantId);
}
