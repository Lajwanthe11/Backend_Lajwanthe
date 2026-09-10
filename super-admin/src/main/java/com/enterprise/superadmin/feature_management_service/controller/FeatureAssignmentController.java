package com.enterprise.superadmin.feature_management_service.controller;

import com.enterprise.superadmin.feature_management_service.dto.request.FeatureAssignmentRequest;
import com.enterprise.superadmin.feature_management_service.dto.response.FeatureAssignmentResponse;
import com.enterprise.superadmin.feature_management_service.services.FeatureAssignmentService;
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
public class FeatureAssignmentController {

    private static final Logger log = LoggerFactory.getLogger(FeatureAssignmentController.class);
    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final FeatureAssignmentService featureAssignmentService;

    public FeatureAssignmentController(FeatureAssignmentService featureAssignmentService) {
        this.featureAssignmentService = featureAssignmentService;
    }

    @PostMapping("/assign")
    public ResponseEntity<FeatureAssignmentResponse> assignFeature(
            @Valid @RequestBody FeatureAssignmentRequest request,
            Authentication authentication) {

        UUID userId = getUserId(authentication);
        log.info("REST request to assign featureId: {} to tenantId: {} by user: {}", request.getFeatureId(), request.getTenantId(), userId);

        if (request.getCreatedBy() == null) {
            request.setCreatedBy(userId);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(featureAssignmentService.assignFeature(request));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<FeatureAssignmentResponse>> getTenantFeatures(
            @PathVariable UUID tenantId) {

        log.info("REST request to get feature assignments for tenantId: {}", tenantId);
        return ResponseEntity.ok(featureAssignmentService.getByTenant(tenantId));
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