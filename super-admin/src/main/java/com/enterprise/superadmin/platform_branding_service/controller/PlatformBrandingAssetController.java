package com.enterprise.superadmin.platform_branding_service.controller;

import com.enterprise.superadmin.platform_branding_service.service.BrandingAssetService;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/branding/assets")
public class PlatformBrandingAssetController {

    private static final Logger log = LoggerFactory.getLogger(PlatformBrandingAssetController.class);

    private final BrandingAssetService brandingAssetService;

    public PlatformBrandingAssetController(BrandingAssetService brandingAssetService) {
        this.brandingAssetService = brandingAssetService;
    }

    // =====================================================================
    // LOGO
    // =====================================================================

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> uploadLogo(@RequestPart("file") @NotNull MultipartFile file) {

        log.info("POST /api/v1/branding/assets/logo");
        String reference = brandingAssetService.uploadLogo(file);
        return ResponseEntity.ok(reference);
    }


    @PutMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> replaceLogo(@RequestParam("existingReference") String existingReference, @RequestPart("file") @NotNull MultipartFile file) {

        log.info("PUT /api/v1/branding/assets/logo");
        String reference = brandingAssetService.replaceLogo(existingReference, file);
        return ResponseEntity.ok(reference);
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteLogo(@RequestParam("reference") String reference) {

        log.info("DELETE /api/v1/branding/assets/logo");
        brandingAssetService.deleteLogo(reference);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // LOGIN BACKGROUND
    // =====================================================================

    @PostMapping(value = "/login-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> uploadLoginBackground(@RequestPart("file") @NotNull MultipartFile file) {

        log.info("POST /api/v1/branding/assets/login-background");
        String reference = brandingAssetService.uploadLoginBackground(file);
        return ResponseEntity.ok(reference);
    }

    @PutMapping(value = "/login-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> replaceLoginBackground(@RequestParam("existingReference") String existingReference, @RequestPart("file") @NotNull MultipartFile file) {

        log.info("PUT /api/v1/branding/assets/login-background");
        String reference = brandingAssetService.replaceLoginBackground(existingReference, file);
        return ResponseEntity.ok(reference);
    }

    @DeleteMapping("/login-background")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteLoginBackground(@RequestParam("reference") String reference) {

        log.info("DELETE /api/v1/branding/assets/login-background");
        brandingAssetService.deleteLoginBackground(reference);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // FAVICON
    // =====================================================================

    @PostMapping(value = "/favicon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> uploadFavicon(@RequestPart("file") @NotNull MultipartFile file) {

        log.info("POST /api/v1/branding/assets/favicon");
        String reference = brandingAssetService.uploadFavicon(file);
        return ResponseEntity.ok(reference);
    }

    @PutMapping(value = "/favicon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> replaceFavicon(@RequestParam("existingReference") String existingReference, @RequestPart("file") @NotNull MultipartFile file) {

        log.info("PUT /api/v1/branding/assets/favicon");
        String reference = brandingAssetService.replaceFavicon(existingReference, file);
        return ResponseEntity.ok(reference);
    }

    @DeleteMapping("/favicon")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFavicon(@RequestParam("reference") String reference) {

        log.info("DELETE /api/v1/branding/assets/favicon");
        brandingAssetService.deleteFavicon(reference);
        return ResponseEntity.noContent().build();
    }

    // =====================================================================
    // EMAIL HEADER LOGO
    // =====================================================================

    @PostMapping(value = "/email-header-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> uploadEmailHeaderLogo(@RequestPart("file") @NotNull MultipartFile file) {

        log.info("POST /api/v1/branding/assets/email-header-logo");
        String reference = brandingAssetService.uploadEmailHeaderLogo(file);
        return ResponseEntity.ok(reference);
    }

    @PutMapping(value = "/email-header-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> replaceEmailHeaderLogo(@RequestParam("existingReference") String existingReference, @RequestPart("file") @NotNull MultipartFile file) {

        log.info("PUT /api/v1/branding/assets/email-header-logo");
        String reference = brandingAssetService.replaceEmailHeaderLogo(existingReference, file);
        return ResponseEntity.ok(reference);
    }

    @DeleteMapping("/email-header-logo")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteEmailHeaderLogo(@RequestParam("reference") String reference) {

        log.info("DELETE /api/v1/branding/assets/email-header-logo");
        brandingAssetService.deleteEmailHeaderLogo(reference);
        return ResponseEntity.noContent().build();
    }
}