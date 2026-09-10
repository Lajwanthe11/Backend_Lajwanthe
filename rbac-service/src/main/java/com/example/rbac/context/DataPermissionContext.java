package com.example.rbac.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPermissionContext {

    private UUID userId;
    private UUID tenantId;
    @Builder.Default
    private Set<UUID> roleIds = new HashSet<>();
    private boolean superAdmin;
    private boolean systemRole;
    private String departmentId;
    private String branchId;
    private String organizationId;

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    private static final ThreadLocal<DataPermissionContext> CURRENT_CONTEXT = new InheritableThreadLocal<>();

    public static DataPermissionContext get() {
        return CURRENT_CONTEXT.get();
    }

    public static void set(DataPermissionContext context) {
        CURRENT_CONTEXT.set(context);
    }

    public static void clear() {
        CURRENT_CONTEXT.remove();
    }

    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    public void setAttribute(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(key, value);
    }
}
