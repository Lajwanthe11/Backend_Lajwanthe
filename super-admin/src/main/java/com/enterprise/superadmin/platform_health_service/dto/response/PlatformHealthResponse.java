package com.enterprise.superadmin.platform_health_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformHealthResponse {

    private String status;

    private boolean availability;

    private LocalDateTime retrievedAt;

    private List<ServiceHealthResponse> services;
}