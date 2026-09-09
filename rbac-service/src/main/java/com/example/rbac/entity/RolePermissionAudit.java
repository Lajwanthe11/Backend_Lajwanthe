package com.example.rbac.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "role_permission_audit")
@Getter
@Setter
@NoArgsConstructor
public class RolePermissionAudit {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "from_granted", nullable = false)
    private boolean fromGranted;

    @Column(name = "to_granted", nullable = false)
    private boolean toGranted;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}