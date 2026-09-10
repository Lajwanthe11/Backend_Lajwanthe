package com.enterprise.superadmin.platform_health_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthResponse {

    private String serviceName;

    private String status;

    private boolean availability;

    private Long responseTimeMs;
}