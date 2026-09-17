package com.example.microservice.common.tenant;

/**
 * Thread-safe context holder for the current tenant identifier.
 */
public final class TenantContext {

    public static final String DEFAULT_TENANT_ID = "default";
    public static final String TENANT_HEADER = "X-Tenant-ID";

    private static final ThreadLocal<String> CURRENT_TENANT = new InheritableThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    public static String getTenantId() {
        String tenantId = CURRENT_TENANT.get();
        return (tenantId != null && !tenantId.isBlank()) ? tenantId : DEFAULT_TENANT_ID;
    }

    public static void setTenantId(String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            CURRENT_TENANT.set(tenantId.trim());
        } else {
            CURRENT_TENANT.set(DEFAULT_TENANT_ID);
        }
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
