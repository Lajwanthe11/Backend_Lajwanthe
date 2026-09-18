package com.example.rbac.dto;

import java.util.Map;

// Dto returned by the frontend/ api/v1/module-access endpoint.
// It is used to check if the user has access to the module and features.
public record ModuleAccess(boolean canAccess, Map<String, Boolean> features) {
}
