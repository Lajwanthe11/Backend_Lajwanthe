package com.example.platformadmin.organizations.businessunit.controller;

import com.example.common.abstracts.AbstractController;
import com.example.platformadmin.organizations.businessunit.dto.BusinessUnitRequestDto;
import com.example.platformadmin.organizations.businessunit.dto.BusinessUnitResponseDto;
import com.example.platformadmin.organizations.businessunit.entity.BusinessUnit;
import com.example.platformadmin.organizations.businessunit.service.BusinessUnitServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Business Unit management.
 * Inherits full CRUD from {@link AbstractController}.
 */
@RestController
@RequestMapping("/business-units")
@Tag(name = "Business Units", description = "CRUD operations for Business Units")
public class BusinessUnitController extends AbstractController<
        BusinessUnit,
        Long,
        BusinessUnitRequestDto,
        BusinessUnitResponseDto> {

    private final BusinessUnitServiceImpl businessUnitService;

    public BusinessUnitController(BusinessUnitServiceImpl businessUnitService) {
        super(businessUnitService);
        this.businessUnitService = businessUnitService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search business units", description = "Case-insensitive keyword search across code and name")
    public ResponseEntity<List<BusinessUnitResponseDto>> searchBusinessUnits(@RequestParam String query) {
        return ResponseEntity.ok(businessUnitService.searchBusinessUnits(query));
    }
}
