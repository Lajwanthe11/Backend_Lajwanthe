package com.example.platformadmin.organizations.costcenter.controller;

import com.example.common.abstracts.AbstractController;
import com.example.common.response.ApiResponse;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
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

@RestController
@RequestMapping("/cost-centers")
@Tag(name = "Cost Center Management", description = "CRUD and budget management APIs for departmental cost centers")
@SecurityRequirement(name = "bearerAuth")
public class CostCenterController extends AbstractController<
        CostCenterEntity,
        UUID,
        CostCenterRequestDTO,
        CostCenterResponseDTO> {

    private final CostCenterService costCenterService;

    public CostCenterController(CostCenterService costCenterService) {
        super(costCenterService);
        this.costCenterService = costCenterService;
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get cost center by code", description = "Retrieves a cost center by its unique business code")
    public ResponseEntity<ApiResponse<CostCenterResponseDTO>> getByCode(@PathVariable String code) {
        CostCenterResponseDTO dto = costCenterService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Get cost centers by organization", description = "Retrieves all cost centers belonging to an organization")
    public ResponseEntity<ApiResponse<List<CostCenterResponseDTO>>> getByOrganizationId(@PathVariable UUID organizationId) {
        List<CostCenterResponseDTO> list = costCenterService.getByOrganizationId(organizationId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get cost centers by company", description = "Retrieves all cost centers belonging to a company")
    public ResponseEntity<ApiResponse<List<CostCenterResponseDTO>>> getByCompanyId(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.ok(costCenterService.getByCompanyId(companyId)));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get cost centers by department", description = "Retrieves all cost centers associated with a department")
    public ResponseEntity<ApiResponse<List<CostCenterResponseDTO>>> getByDepartmentId(@PathVariable Long departmentId) {
        List<CostCenterResponseDTO> list = costCenterService.getByDepartmentId(departmentId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}