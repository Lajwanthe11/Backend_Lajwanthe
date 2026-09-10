package com.example.rbac.dto;

import java.util.Map;

public record ModuleAccess(boolean canAccess, Map<String, Boolean> features) {
}
