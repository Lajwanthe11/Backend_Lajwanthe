package com.enterprise.superadmin.platform_health_service.controller;

import com.enterprise.superadmin.platform_health_service.dto.response.DatabaseHealthResponse;
import com.enterprise.superadmin.platform_health_service.dto.response.PlatformHealthResponse;
import com.enterprise.superadmin.platform_health_service.dto.response.ServiceHealthResponse;
import com.enterprise.superadmin.platform_health_service.service.DatabaseHealthService;
import com.enterprise.superadmin.platform_health_service.service.PlatformHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Platform Health",
        description = "APIs for monitoring platform and service health"
)
public class PlatformHealthController {

    private final PlatformHealthService platformHealthService;
    private final DatabaseHealthService databaseHealthService;

    @GetMapping
    @Operation(
            summary = "Get overall platform health",
            description = "Returns the aggregated health status of the platform"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Platform health retrieved successfully"
    )
    public ResponseEntity<PlatformHealthResponse> getPlatformHealth() {

        PlatformHealthResponse response =
                platformHealthService.getPlatformHealth();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/services")
    @Operation(
            summary = "Get health of all services",
            description = "Returns health information for approved platform services"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service health information retrieved successfully"
    )
    public ResponseEntity<List<ServiceHealthResponse>> getAllServicesHealth() {

        List<ServiceHealthResponse> response =
                platformHealthService.getAllServicesHealth();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/database")
    @Operation(
            summary = "Get database health",
            description = "Returns the health status of the platform database"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Database health retrieved successfully"
    )
    public ResponseEntity<DatabaseHealthResponse> getDatabaseHealth() {

        DatabaseHealthResponse response =
                databaseHealthService.getDatabaseHealth();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/services/{serviceName}")
    @Operation(
            summary = "Get health of a specific service",
            description = "Returns health information for the requested service"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service health information retrieved successfully"
    )
    public ResponseEntity<ServiceHealthResponse> getServiceHealth(
            @PathVariable
            @Parameter(
                    description = "Name of the service to check",
                    required = true,
                    example = "authentication-service"
            )
            @NotBlank(message = "Service name must not be blank")
            String serviceName) {

        ServiceHealthResponse response =
                platformHealthService.getServiceHealth(serviceName);

        return ResponseEntity.ok(response);
    }
}