package com.example.platformadmin.rbac.service;

import java.util.UUID;

public interface CurrentUserContext {
    UUID getTenantId();
    String getUserId();
    String getUserDisplayName();
    boolean hasRole(String roleCode);
}