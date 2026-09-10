package com.enterprise.superadmin.platform_branding_service.controller;

import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingCreateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingUpdateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.response.BrandingResponse;
import com.enterprise.superadmin.platform_branding_service.service.BrandingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/branding")
public class PlatformBrandingController {

    private static final Logger log = LoggerFactory.getLogger(PlatformBrandingController.class);

    private final BrandingService brandingService;

    public PlatformBrandingController(BrandingService brandingService) {
        this.brandingService = brandingService;
    }

    // =====================================================================
    // GET CURRENT BRANDING
    // =====================================================================

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BrandingResponse> getCurrentBranding() {

        log.info("GET /api/v1/branding");
        BrandingResponse response = brandingService.getCurrentBranding();
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    // CREATE / INITIALIZE BRANDING
    // =====================================================================

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BrandingResponse> createBranding(@Valid @RequestBody BrandingCreateRequest request) {

        log.info("POST /api/v1/branding");
        BrandingResponse response = brandingService.createBranding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================================================================
    // UPDATE BRANDING
    // =====================================================================

    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BrandingResponse> updateBranding(@Valid @RequestBody BrandingUpdateRequest request) {

        log.info("PUT /api/v1/branding");
        BrandingResponse response = brandingService.updateBranding(request);
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    // RESET BRANDING
    // =====================================================================

    @PostMapping("/reset")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BrandingResponse> resetBranding() {

        log.info("POST /api/v1/branding/reset");
        BrandingResponse response = brandingService.resetBranding();
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    // PREVIEW BRANDING
    // =====================================================================

    @PostMapping("/preview")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BrandingResponse> previewBranding(@Valid @RequestBody BrandingUpdateRequest request) {

        log.info("POST /api/v1/branding/preview");
        BrandingResponse response = brandingService.previewBranding(request);
        return ResponseEntity.ok(response);
    }

    // =====================================================================
    // PUBLISH BRANDING
    // =====================================================================

    @PostMapping("/publish")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BrandingResponse> publishBranding() {

        log.info("POST /api/v1/branding/publish");
        BrandingResponse response = brandingService.publishBranding();
        return ResponseEntity.ok(response);
    }
}