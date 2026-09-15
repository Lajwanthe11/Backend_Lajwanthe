package com.example.platformadmin.organizations.branches.controller;

import com.example.common.abstracts.AbstractController;
import com.example.platformadmin.organizations.branches.dto.BranchRequestDTO;
import com.example.platformadmin.organizations.branches.dto.BranchResponseDTO;
import com.example.platformadmin.organizations.branches.entity.BranchEntity;
import com.example.platformadmin.organizations.branches.service.BranchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/branches")
@SecurityRequirement(name = "bearerAuth")
public class BranchController extends AbstractController<
        BranchEntity,
        Long,
        BranchRequestDTO,
        BranchResponseDTO> {

    public BranchController(BranchService branchService) {
        super(branchService);
    }
}