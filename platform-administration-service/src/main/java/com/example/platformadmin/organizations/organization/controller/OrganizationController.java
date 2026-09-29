package com.example.platformadmin.organizations.organization.controller;

import com.example.common.abstracts.AbstractController;
import com.example.common.response.ApiResponse;
import com.example.platformadmin.organizations.organization.dto.OrganizationRequestDTO;
import com.example.platformadmin.organizations.organization.dto.OrganizationResponseDTO;
import com.example.platformadmin.organizations.organization.entity.OrganizationEntity;
import com.example.platformadmin.organizations.organization.enums.OrganizationType;
import com.example.platformadmin.organizations.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/organizations")
@Tag(name = "Organization Management", description = "CRUD and search APIs for Organization setup and onboarding")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController extends AbstractController<
        OrganizationEntity,
        UUID,
        OrganizationRequestDTO,
        OrganizationResponseDTO> {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        super(organizationService);
        this.organizationService = organizationService;
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get organization by code", description = "Retrieves an organization by its unique business code")
    public ResponseEntity<ApiResponse<OrganizationResponseDTO>> getByCode(@PathVariable String code) {
        OrganizationResponseDTO dto = organizationService.getByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/search")
    @Operation(summary = "Search organizations", description = "Case-insensitive keyword search across code, name, industry, city, and country")
    public ResponseEntity<ApiResponse<List<OrganizationResponseDTO>>> search(@RequestParam String query) {
        List<OrganizationResponseDTO> results = organizationService.searchOrganizations(query);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Filter organizations by type", description = "Retrieves all organizations matching the specified type")
    public ResponseEntity<ApiResponse<List<OrganizationResponseDTO>>> getByType(@PathVariable String type) {
        OrganizationType orgType = parseOrganizationType(type);
        List<OrganizationResponseDTO> results = organizationService.getByType(orgType);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    private OrganizationType parseOrganizationType(String type) {
        for (OrganizationType ot : OrganizationType.values()) {
            if (ot.name().equalsIgnoreCase(type)
                    || ot.name().replace("_", "-").equalsIgnoreCase(type)
                    || ot.getDisplayName().equalsIgnoreCase(type)) {
                return ot;
            }
        }
        // Throws Bad Request if an invalid type like "MARKIV" is passed
        throw new IllegalArgumentException(
                String.format("Invalid organization type '%s'. Allowed types are: %s",
                        type, java.util.Arrays.stream(OrganizationType.values())
                                .map(Enum::name)
                                .collect(java.util.stream.Collectors.joining(", "))));
    }
}