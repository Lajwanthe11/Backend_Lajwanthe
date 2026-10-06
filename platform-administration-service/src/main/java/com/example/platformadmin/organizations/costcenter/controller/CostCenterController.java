package com.example.platformadmin.organizations.costcenter.controller;

import com.example.common.abstracts.AbstractController;
import com.example.common.response.ApiResponse;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.enums.CostCenterStatus;
import com.example.platformadmin.organizations.costcenter.service.CostCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Cost Center management.
 * Handles Cost Center search operations and inherits
 * standard CRUD operations from AbstractController.
 */
@RestController
@RequestMapping("/cost-centers")
@Tag(
        name = "Cost Centers",
        description = "CRUD, soft delete and search operations for Cost Centers"
)
@SecurityRequirement(name = "bearerAuth")
public class CostCenterController extends AbstractController<
        CostCenterEntity,
        Long,
        CostCenterRequestDTO,
        CostCenterResponseDTO> {

    private final CostCenterService costCenterService;

    public CostCenterController(
            CostCenterService costCenterService) {

        super(costCenterService);
        this.costCenterService = costCenterService;
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Cost Centers by status")
    public ResponseEntity<ApiResponse<List<CostCenterResponseDTO>>> getByStatus(
            @PathVariable String status) {

        CostCenterStatus costCenterStatus;

        try {
            costCenterStatus =
                    CostCenterStatus.valueOf(
                            status.trim().toUpperCase()
                    );
        } catch (IllegalArgumentException ex) {

            throw new IllegalArgumentException(
                    "Invalid Cost Center status: " + status
                            + ". Allowed values are ACTIVE, INACTIVE, FROZEN"
            );
        }

        List<CostCenterResponseDTO> results =
                costCenterService.getByStatus(costCenterStatus);

        return ResponseEntity.ok(
                ApiResponse.ok(results)
        );
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get Cost Centers by organization ID")
    public ResponseEntity<ApiResponse<List<CostCenterResponseDTO>>>
    getByOrganizationId(
            @PathVariable UUID organizationId) {

        List<CostCenterResponseDTO> results =
                costCenterService.getByOrganizationId(
                        organizationId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(results)
        );
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get Cost Centers by company ID")
    public ResponseEntity<ApiResponse<List<CostCenterResponseDTO>>>
    getByCompanyId(
            @PathVariable Long companyId) {

        List<CostCenterResponseDTO> results =
                costCenterService.getByCompanyId(companyId);

        return ResponseEntity.ok(
                ApiResponse.ok(results)
        );
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get Cost Centers by department ID")
    public ResponseEntity<ApiResponse<List<CostCenterResponseDTO>>>
    getByDepartmentId(
            @PathVariable Long departmentId) {

        List<CostCenterResponseDTO> results =
                costCenterService.getByDepartmentId(
                        departmentId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(results)
        );
    }
}


