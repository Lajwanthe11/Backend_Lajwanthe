package com.enterprise.superadmin.feature_management_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Data
@Table(
        name = "features",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_features_feature_name",
                        columnNames = "feature_name"
                )
        },
        indexes = {
                @Index(name = "idx_features_module", columnList = "module"),
                @Index(name = "idx_features_license_plan", columnList = "license_plan"),
                @Index(name = "idx_features_status", columnList = "status")
        }
)

public class Feature {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(
            name = "feature_name",
            nullable = false,
            length = 150,
            unique = true
    )
    private String featureName;
    @Column(
            name = "module",
            nullable = false,
            length = 100
    )
    private String module;
    @Column(
            name = "license_plan",
            nullable = false,
            length = 100
    )
    private String licensePlan;
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private String status = "DISABLED";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "configuration",
            columnDefinition = "jsonb"
    )
    private String configuration;
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

    public Feature() {

    }


    @PrePersist

    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;

        updatedAt = now;

        if (status == null) {

            status = "DISABLED";

        }

    }

    @PreUpdate

    protected void onUpdate() {

        updatedAt = LocalDateTime.now();

    }

}
