package com.example.platformadmin.superadmin.platform_health_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseHealthResponse {

    private String status;

    private boolean availability;

}