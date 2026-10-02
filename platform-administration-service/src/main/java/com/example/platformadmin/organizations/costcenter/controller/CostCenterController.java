package com.example.platformadmin.organizations.costcenter.controller;

import com.example.common.abstracts.AbstractController;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterRequestDTO;
import com.example.platformadmin.organizations.costcenter.dto.CostCenterResponseDTO;
import com.example.platformadmin.organizations.costcenter.entity.CostCenterEntity;
import com.example.platformadmin.organizations.costcenter.service.CostCenterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Cost Center management.
 * Handles HTTP requests for creating, retrieving,
 * updating, and deleting Cost Centers.
 */

@RestController
@RequestMapping("/cost-centers")
public class CostCenterController extends AbstractController<
        CostCenterEntity,
        Long,
        CostCenterRequestDTO,
        CostCenterResponseDTO> {

    public CostCenterController(CostCenterService costCenterService) {
        super(costCenterService);
    }

    @GetMapping("/test")
    public String test() {
        return "Cost Center Controller is working";
    }
}