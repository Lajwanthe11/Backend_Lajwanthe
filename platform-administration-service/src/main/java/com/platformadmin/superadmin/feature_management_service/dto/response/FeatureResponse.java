package com.example.platformadmin.superadmin.feature_management_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

import java.util.UUID;
@Data
public class FeatureResponse {

    private UUID id;

    private String featureName;

    private String module;

    private String licensePlan;

    private String status;

    private String configuration;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UUID createdBy;

    private UUID updatedBy;

}
