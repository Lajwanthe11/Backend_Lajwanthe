package com.example.platformadmin.organizations.company.controller;

import com.example.common.abstracts.AbstractController;
import com.example.platformadmin.organizations.company.dto.CompanyRequestDto;
import com.example.platformadmin.organizations.company.dto.CompanyResponseDto;
import com.example.platformadmin.organizations.company.entity.Company;
import com.example.platformadmin.organizations.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Company management.
 * Inherits full CRUD from {@link AbstractController}.
 */
@RestController
@RequestMapping("/companies")
@Tag(name = "Companies", description = "CRUD operations for Companies")
public class CompanyController extends AbstractController<
        Company,
        Long,
        CompanyRequestDto,
        CompanyResponseDto> {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        super(companyService);
        this.companyService = companyService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search companies", description = "Case-insensitive keyword search across code, name, and email")
    public ResponseEntity<List<CompanyResponseDto>> searchCompanies(@RequestParam String query) {
        return ResponseEntity.ok(companyService.searchCompanies(query));
    }
}
