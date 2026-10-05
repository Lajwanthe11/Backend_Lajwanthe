package com.example.platformadmin.organizations.costcenter.controller;

import com.example.common.response.ApiResponse;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.service.CostCenterService;

import com.example.common.abstracts.AbstractController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cost-centers")
@Tag(
        name = "Cost Center Management",
        description = "CRUD and search APIs for Cost Center management"
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

    // =========================================================
    // GET BY CODE
    // =========================================================

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CostCenterResponseDTO>>
    getByCode(
            @PathVariable String code) {

        CostCenterResponseDTO result =
                costCenterService.getByCode(code);

        return ResponseEntity.ok(
                ApiResponse.ok(result)
        );
    }

    // =========================================================
    // GET BY ORGANIZATION
    // =========================================================

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<
            ApiResponse<List<CostCenterResponseDTO>>>
    getByOrganizationId(
            @PathVariable UUID organizationId) {

        List<CostCenterResponseDTO> result =
                costCenterService.getByOrganizationId(
                        organizationId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(result)
        );
    }

    // =========================================================
    // GET BY COMPANY
    // =========================================================

    @GetMapping("/company/{companyId}")
    public ResponseEntity<
            ApiResponse<List<CostCenterResponseDTO>>>
    getByCompanyId(
            @PathVariable Long companyId) {

        List<CostCenterResponseDTO> result =
                costCenterService.getByCompanyId(
                        companyId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(result)
        );
    }

    // =========================================================
    // GET BY DEPARTMENT
    // =========================================================

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<
            ApiResponse<List<CostCenterResponseDTO>>>
    getByDepartmentId(
            @PathVariable Long departmentId) {

        List<CostCenterResponseDTO> result =
                costCenterService.getByDepartmentId(
                        departmentId
                );

        return ResponseEntity.ok(
                ApiResponse.ok(result)
        );
    }
}