package com.example.platformadmin.organizations.location.controller;

import com.example.common.abstracts.AbstractController;
import com.example.common.response.ApiResponse;
import com.example.common.response.PageResponse;
import com.example.platformadmin.organizations.location.dto.LocationRequestDto;
import com.example.platformadmin.organizations.location.dto.LocationResponseDto;
import com.example.platformadmin.organizations.location.entity.LocationEntity;
import com.example.platformadmin.organizations.location.service.LocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController extends AbstractController<
        LocationEntity,
        Long,
        LocationRequestDto,
        LocationResponseDto> {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        super(locationService);
        this.locationService = locationService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<LocationResponseDto>>> search(
            @RequestParam String query,
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<LocationResponseDto> page =
                locationService.search(query, pageable);

        return ResponseEntity.ok(
                ApiResponse.ok(PageResponse.from(page))
        );
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<LocationResponseDto>>> getByCompany(
            @PathVariable Long companyId) {

        List<LocationResponseDto> locations =
                locationService.getByCompanyId(companyId);

        return ResponseEntity.ok(
                ApiResponse.ok(locations)
        );
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<ApiResponse<List<LocationResponseDto>>> getByBranch(
            @PathVariable Long branchId) {

        List<LocationResponseDto> locations =
                locationService.getByBranchId(branchId);

        return ResponseEntity.ok(
                ApiResponse.ok(locations)
        );
    }
}