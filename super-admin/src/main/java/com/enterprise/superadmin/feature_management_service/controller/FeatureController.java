package com.enterprise.superadmin.feature_management_service.controller;

import com.enterprise.superadmin.feature_management_service.dto.request.FeatureCreateRequest;
import com.enterprise.superadmin.feature_management_service.dto.request.FeatureUpdateRequest;
import com.enterprise.superadmin.feature_management_service.dto.response.FeatureResponse;
import com.enterprise.superadmin.feature_management_service.services.FeatureService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/features")
public class FeatureController {

    private static final Logger log = LoggerFactory.getLogger(FeatureController.class);
    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final FeatureService featureService;

    public FeatureController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @PostMapping
    public ResponseEntity<FeatureResponse> createFeature(
            @Valid @RequestBody FeatureCreateRequest request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        log.info("REST request to create feature: {} by user: {}", request.getFeatureName(), userId);

        if (request.getCreatedBy() == null || request.getCreatedBy().trim().isEmpty()) {
            request.setCreatedBy(userId.toString());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(featureService.createFeature(request));
    }

    @GetMapping
    public ResponseEntity<List<FeatureResponse>> getAllFeatures() {
        log.info("REST request to get all features");
        return ResponseEntity.ok(featureService.getAllFeatures());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponse> updateFeature(
            @PathVariable UUID id,
            @Valid @RequestBody FeatureUpdateRequest request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        log.info("REST request to update feature id: {} by user: {}", id, userId);

        if (request.getUpdatedBy() == null || request.getUpdatedBy().trim().isEmpty()) {
            request.setUpdatedBy(userId.toString());
        }

        return ResponseEntity.ok(featureService.updateFeature(id, request, userId));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<FeatureResponse> activateFeature(
            @PathVariable UUID id,
            Authentication authentication) {

        log.info("REST request to activate feature id: {}", id);
        return ResponseEntity.ok(featureService.enableFeature(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<FeatureResponse> deactivateFeature(
            @PathVariable UUID id,
            Authentication authentication) {

        log.info("REST request to deactivate feature id: {}", id);
        return ResponseEntity.ok(featureService.disableFeature(id));
    }

    private UUID getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return DEFAULT_USER_ID;
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            return DEFAULT_USER_ID;
        }
    }
}

