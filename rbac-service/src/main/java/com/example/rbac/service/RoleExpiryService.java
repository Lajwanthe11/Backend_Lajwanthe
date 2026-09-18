package com.example.rbac.service;

import com.example.rbac.dto.ExpiryNotificationResponse;

import java.util.UUID;

public interface RoleExpiryService {
    ExpiryNotificationResponse triggerForTenant(UUID tenantId);
}
