package com.example.platformadmin.superadmin.superadmindashboard.dto.response;

import lombok.Data;

@Data
public class OperationalStatisticsResponse {

    private Double cpuUtilization;
    private Double memoryUsage;
    private Double storageUtilization;
    private Long apiRequests;
    private Long backgroundJobs;
    private Long failedJobs;
}
