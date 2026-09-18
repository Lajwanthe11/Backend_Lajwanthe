package com.example.rbac.service;

public interface CurrentUserContext {
    String getTenantId();
    String getUserId();
    String getUserDisplayName();
    boolean hasRole(String roleCode);
}