package com.enterprise.superadmin.feature_management_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Data
@Table(name = "feature_assignments",

        uniqueConstraints = {
                @UniqueConstraint(name = "uk_feature_tenant", columnNames = {"feature_id", "tenant_id"})
        },
        indexes = {
                @Index(name = "idx_feature_assignments_feature_id", columnList = "feature_id"),

                @Index(name = "idx_feature_assignments_tenant_id", columnList = "tenant_id"),

                @Index(name = "idx_feature_assignments_organization_id", columnList = "organization_id"),

                @Index(name = "idx_feature_assignments_license_plan", columnList = "license_plan"),

                @Index(name = "idx_feature_assignments_status", columnList = "status")
        }
)

public class FeatureAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "feature_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_feature_assignments_feature"
            )
    )
    private Feature feature;
    @Column(
            name = "tenant_id",
            nullable = false
    )
    private UUID tenantId;
    @Column(name = "organization_id")
    private UUID organizationId;
    @Column(
            name = "license_plan",
            length = 100
    )
    private String licensePlan;
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private String status = "ENABLED";
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "configuration",
            columnDefinition = "jsonb"
    )

    private String configuration;
    @Column(
            name = "assigned_at",
            nullable = false
    )
    private LocalDateTime assignedAt;
    @Column(name = "unassigned_at")
    private LocalDateTime unassignedAt;
    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "updated_by")
    private UUID updatedBy;
    // Constructors
    public FeatureAssignment() {

    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (assignedAt == null) {
            assignedAt = now;
        }
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = "ENABLED";

        }

    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

    }

}
