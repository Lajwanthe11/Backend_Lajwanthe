package com.enterprise.superadmin.feature_management_service.dto.request;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeatureCreateRequest {

    @NotBlank(message = "Feature name is required")

    @Size(max = 150, message = "Feature name must not exceed 150 characters")

    private String featureName;

    @NotBlank(message = "Module is required")

    @Size(max = 100, message = "Module must not exceed 100 characters")

    private String module;

    @NotBlank(message = "License plan is required")

    @Size(max = 100, message = "License plan must not exceed 100 characters")

    private String licensePlan;

    @NotBlank(message = "Status is required")

    private String status;

    private String configuration;

    private String createdBy;

}
