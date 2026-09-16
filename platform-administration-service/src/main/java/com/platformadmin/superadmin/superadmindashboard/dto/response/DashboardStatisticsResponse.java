package com.example.platformadmin.superadmin.superadmindashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {

    private String serverHealth;
    private String apiGatewayStatus;
    private String databaseStatus;

    private double storageUtilizationPercent;
    private double cpuUsagePercent;
    private double memoryUsagePercent;

    private Instant lastUpdated;
}