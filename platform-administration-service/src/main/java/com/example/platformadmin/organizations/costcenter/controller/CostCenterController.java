package com.example.platformadmin.organizations.costcenter.controller;

import com.example.common.abstracts.AbstractController;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.service.CostCenterService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cost-centers")
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
    // GET COST CENTERS BY DEPARTMENT ID
    // =========================================================

    @GetMapping("/department/{departmentId}")
    public List<CostCenterResponseDTO> getByDepartmentId(
            @PathVariable Long departmentId) {

        return costCenterService.getByDepartmentId(
                departmentId
        );
    }

    // =========================================================
    // GET COST CENTERS BY COMPANY ID
    // =========================================================

    @GetMapping("/company/{companyId}")
    public List<CostCenterResponseDTO> getByCompanyId(
            @PathVariable Long companyId) {

        return costCenterService.getByCompanyId(
                companyId
        );
    }

    // =========================================================
    // GET COST CENTERS BY ORGANIZATION ID
    // =========================================================

    @GetMapping("/organization/{organizationId}")
    public List<CostCenterResponseDTO> getByOrganizationId(
            @PathVariable UUID organizationId) {

        return costCenterService.getByOrganizationId(
                organizationId
        );
    }

    // =========================================================
    // GET COST CENTER BY CODE
    // =========================================================

    @GetMapping("/code/{code}")
    public CostCenterResponseDTO getByCode(
            @PathVariable String code) {

        return costCenterService.getByCode(code);
    }

    // =========================================================
    // GET COST CENTER BY ID
    // =========================================================

//    @GetMapping("/{costCenterId}")
//    public CostCenterResponseDTO getByCostCenterId(
//            @PathVariable Long costCenterId) {
//
//        return costCenterService.getByCostCenterId(
//                costCenterId
//        );
//    }
}