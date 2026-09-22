package com.example.platformadmin.superadmin.feature_management_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

import java.util.UUID;
@Data
public class FeatureAssignmentResponse {

    private UUID id;

    private UUID featureId;

    private UUID tenantId;

    private UUID organizationId;

    private String licensePlan;

    private String status;

    private String configuration;

    private LocalDateTime assignedAt;

    private LocalDateTime unassignedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UUID createdBy;

    private UUID updatedBy;


}
