package com.enterprise.superadmin.feature_management_service.services;

import com.enterprise.superadmin.feature_management_service.dto.request.FeatureCreateRequest;
import com.enterprise.superadmin.feature_management_service.dto.request.FeatureUpdateRequest;
import com.enterprise.superadmin.feature_management_service.dto.response.FeatureResponse;
import com.enterprise.superadmin.feature_management_service.entity.Feature;
import com.enterprise.superadmin.feature_management_service.exception.FeatureConfigurationException;
import com.enterprise.superadmin.feature_management_service.exception.FeatureNotFoundException;
import com.enterprise.superadmin.feature_management_service.exception.InvalidFeatureStateException;
import com.enterprise.superadmin.feature_management_service.repository.FeatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeatureService {

    private static final Logger log = LoggerFactory.getLogger(FeatureService.class);
    private final FeatureRepository featureRepository;

    public FeatureService(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    // CREATE

    public FeatureResponse createFeature(FeatureCreateRequest request) {
        log.info("Creating new feature with name: {}", request.getFeatureName());

        if (featureRepository.existsByFeatureName(request.getFeatureName())) {
            log.warn("Feature creation failed. Feature already exists: {}", request.getFeatureName());
            throw new InvalidFeatureStateException(
                    "Feature already exists: " + request.getFeatureName()
            );
        }

        validateStatus(request.getStatus());

        Feature feature = new Feature();

        feature.setFeatureName(request.getFeatureName());
        feature.setModule(request.getModule());
        feature.setLicensePlan(request.getLicensePlan());
        feature.setStatus(request.getStatus());

        validateConfiguration(request.getConfiguration());
        feature.setConfiguration(request.getConfiguration());

        if (request.getCreatedBy() != null && !request.getCreatedBy().trim().isEmpty()) {
            try {
                feature.setCreatedBy(UUID.fromString(request.getCreatedBy()));
            } catch (IllegalArgumentException e) {
                feature.setCreatedBy(UUID.fromString("00000000-0000-0000-0000-000000000001"));
            }
        }

        Feature savedFeature = featureRepository.save(feature);
        log.info("Feature created successfully with id: {}", savedFeature.getId());

        return mapToResponse(savedFeature);
    }

    // GET ALL

    @Transactional(readOnly = true)
    public List<FeatureResponse> getAllFeatures() {
        log.info("Retrieving all features");
        return featureRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID

    @Transactional(readOnly = true)
    public FeatureResponse getFeatureById(UUID id) {
        log.info("Retrieving feature by id: {}", id);
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Feature not found with id: {}", id);
                    return new FeatureNotFoundException(
                            "Feature not found with id: " + id
                    );
                });

        return mapToResponse(feature);
    }

    // UPDATE

    public FeatureResponse updateFeature(
            UUID id,
            FeatureUpdateRequest request,
            UUID userId) {

        log.info("Updating feature with id: {}", id);
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed. Feature not found with id: {}", id);
                    return new FeatureNotFoundException(
                            "Feature not found with id: " + id
                    );
                });

        if (request.getFeatureName() != null) {
            feature.setFeatureName(request.getFeatureName());
        }

        if (request.getModule() != null) {
            feature.setModule(request.getModule());
        }

        if (request.getLicensePlan() != null) {
            feature.setLicensePlan(request.getLicensePlan());
        }

        if (request.getStatus() != null) {
            validateStatus(request.getStatus());
            feature.setStatus(request.getStatus());
        }

        if (request.getConfiguration() != null) {
            validateConfiguration(request.getConfiguration());
            feature.setConfiguration(request.getConfiguration());
        }

        if (request.getUpdatedBy() != null && !request.getUpdatedBy().trim().isEmpty()) {
            try {
                feature.setUpdatedBy(UUID.fromString(request.getUpdatedBy()));
            } catch (IllegalArgumentException e) {
                feature.setUpdatedBy(userId != null ? userId : UUID.fromString("00000000-0000-0000-0000-000000000001"));
            }
        }

        Feature updatedFeature = featureRepository.save(feature);
        log.info("Feature updated successfully for id: {}", updatedFeature.getId());

        return mapToResponse(updatedFeature);
    }

    // ENABLE

    public FeatureResponse enableFeature(UUID id) {
        log.info("Enabling feature with id: {}", id);
        Feature feature = getFeatureEntity(id);

        if ("ENABLED".equals(feature.getStatus())) {
            log.warn("Feature with id: {} is already enabled", id);
            throw new InvalidFeatureStateException(
                    "Feature is already enabled"
            );
        }

        feature.setStatus("ENABLED");
        Feature saved = featureRepository.save(feature);
        log.info("Feature enabled successfully for id: {}", id);

        return mapToResponse(saved);
    }

    // DISABLE

    public FeatureResponse disableFeature(UUID id) {
        log.info("Disabling feature with id: {}", id);
        Feature feature = getFeatureEntity(id);

        if ("DISABLED".equals(feature.getStatus())) {
            log.warn("Feature with id: {} is already disabled", id);
            throw new InvalidFeatureStateException(
                    "Feature is already disabled"
            );
        }

        feature.setStatus("DISABLED");
        Feature saved = featureRepository.save(feature);
        log.info("Feature disabled successfully for id: {}", id);

        return mapToResponse(saved);
    }

    // DELETE

    public void deleteFeature(UUID id) {
        log.info("Deleting feature with id: {}", id);
        Feature feature = getFeatureEntity(id);

        featureRepository.delete(feature);
        log.info("Feature deleted successfully for id: {}", id);
    }

    // FIND ENTITY

    private Feature getFeatureEntity(UUID id) {

        return featureRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Feature entity not found with id: {}", id);
                    return new FeatureNotFoundException(
                            "Feature not found with id: " + id
                    );
                });
    }

    // STATUS VALIDATION

    private void validateStatus(String status) {

        if (!"ENABLED".equals(status)
                && !"DISABLED".equals(status)) {

            throw new InvalidFeatureStateException(
                    "Status must be ENABLED or DISABLED"
            );
        }
    }

    // CONFIGURATION VALIDATION

    private void validateConfiguration(String configuration) {

        if (configuration == null ||
                configuration.trim().isEmpty()) {
            return;
        }

        if (!configuration.trim().startsWith("{")
                || !configuration.trim().endsWith("}")) {

            throw new FeatureConfigurationException(
                    "Configuration must be a valid JSON object"
            );
        }
    }

    // ENTITY → DTO

    private FeatureResponse mapToResponse(Feature feature) {

        FeatureResponse response = new FeatureResponse();

        response.setId(feature.getId());
        response.setFeatureName(feature.getFeatureName());
        response.setModule(feature.getModule());
        response.setLicensePlan(feature.getLicensePlan());
        response.setStatus(feature.getStatus());
        response.setConfiguration(feature.getConfiguration());
        response.setCreatedAt(feature.getCreatedAt());
        response.setUpdatedAt(feature.getUpdatedAt());
        response.setCreatedBy(feature.getCreatedBy());
        response.setUpdatedBy(feature.getUpdatedBy());

        return response;
    }
}