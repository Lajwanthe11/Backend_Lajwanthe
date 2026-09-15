package com.example.platformadmin.superadmin.feature_management_service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeatureUpdateRequest {

    @Size(max = 150, message = "Feature name must not exceed 150 characters")

    private String featureName;

    @Size(max = 100, message = "Module must not exceed 100 characters")

    private String module;

    @Size(max = 100, message = "License plan must not exceed 100 characters")

    private String licensePlan;

    private String status;

    private String configuration;

    private String updatedBy;

}
