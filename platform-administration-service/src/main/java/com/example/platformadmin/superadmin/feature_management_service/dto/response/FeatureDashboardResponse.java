package com.example.platformadmin.superadmin.feature_management_service.dto.response;

public record FeatureDashboardResponse(

        long totalFeatures,

        long enabledFeatures,

        long disabledFeatures
) {
}