package com.enterprise.superadmin.feature_management_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class FeatureAssignmentRequest {

    @NotNull(message = "Feature ID is required")

    private UUID featureId;

    @NotNull(message = "Tenant ID is required")

    private UUID tenantId;

    private UUID organizationId;

    private String licensePlan;

    private String status;

    private String configuration;

    private UUID createdBy;

}
