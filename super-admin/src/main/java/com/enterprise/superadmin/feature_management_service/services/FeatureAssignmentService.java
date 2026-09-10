package com.enterprise.superadmin.feature_management_service.services;

import com.enterprise.superadmin.feature_management_service.dto.request.FeatureAssignmentRequest;
import com.enterprise.superadmin.feature_management_service.dto.response.FeatureAssignmentResponse;
import com.enterprise.superadmin.feature_management_service.entity.Feature;
import com.enterprise.superadmin.feature_management_service.entity.FeatureAssignment;
import com.enterprise.superadmin.feature_management_service.exception.FeatureConfigurationException;
import com.enterprise.superadmin.feature_management_service.exception.FeatureNotFoundException;
import com.enterprise.superadmin.feature_management_service.exception.InvalidFeatureStateException;
import com.enterprise.superadmin.feature_management_service.repository.FeatureAssignmentRepository;
import com.enterprise.superadmin.feature_management_service.repository.FeatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeatureAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(FeatureAssignmentService.class);
    private final FeatureAssignmentRepository assignmentRepository;
    private final FeatureRepository featureRepository;

    public FeatureAssignmentService(
            FeatureAssignmentRepository assignmentRepository,
            FeatureRepository featureRepository) {

        this.assignmentRepository = assignmentRepository;
        this.featureRepository = featureRepository;
    }

    // ASSIGN FEATURE

    public FeatureAssignmentResponse assignFeature(
            FeatureAssignmentRequest request) {

        log.info("Assigning featureId: {} to tenantId: {}", request.getFeatureId(), request.getTenantId());

        Feature feature = featureRepository
                .findById(request.getFeatureId())
                .orElseThrow(() -> {
                    log.warn("Feature not found with id: {}", request.getFeatureId());
                    return new FeatureNotFoundException(
                            "Feature not found with id: " + request.getFeatureId()
                    );
                });

        if (assignmentRepository.existsByFeatureIdAndTenantId(
                request.getFeatureId(),
                request.getTenantId())) {

            log.warn("Feature id: {} already assigned to tenant id: {}", request.getFeatureId(), request.getTenantId());
            throw new InvalidFeatureStateException(
                    "Feature is already assigned to this tenant"
            );
        }

        validateStatus(request.getStatus());

        validateConfiguration(request.getConfiguration());

        FeatureAssignment assignment = new FeatureAssignment();

        assignment.setFeature(feature);
        assignment.setTenantId(request.getTenantId());
        assignment.setOrganizationId(request.getOrganizationId());
        assignment.setLicensePlan(request.getLicensePlan());

        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
        }

        assignment.setConfiguration(request.getConfiguration());
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setCreatedBy(request.getCreatedBy());

        FeatureAssignment saved = assignmentRepository.save(assignment);
        log.info("Feature assignment created successfully with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    // GET ALL

    @Transactional(readOnly = true)
    public List<FeatureAssignmentResponse> getAllAssignments() {
        log.info("Retrieving all feature assignments");
        return assignmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID

    @Transactional(readOnly = true)
    public FeatureAssignmentResponse getAssignmentById(UUID id) {
        log.info("Retrieving assignment by id: {}", id);
        FeatureAssignment assignment = getAssignmentEntity(id);

        return mapToResponse(assignment);
    }

    // GET BY FEATURE

    @Transactional(readOnly = true)
    public List<FeatureAssignmentResponse> getByFeature(
            UUID featureId) {
        log.info("Retrieving assignments for featureId: {}", featureId);
        return assignmentRepository
                .findByFeatureId(featureId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET BY TENANT

    @Transactional(readOnly = true)
    public List<FeatureAssignmentResponse> getByTenant(
            UUID tenantId) {
        log.info("Retrieving assignments for tenantId: {}", tenantId);
        return assignmentRepository
                .findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // DISABLE ASSIGNMENT

    public FeatureAssignmentResponse disableAssignment(UUID id) {
        log.info("Disabling assignment with id: {}", id);
        FeatureAssignment assignment = getAssignmentEntity(id);

        if ("DISABLED".equals(assignment.getStatus())) {
            log.warn("Assignment id: {} is already disabled", id);
            throw new InvalidFeatureStateException(
                    "Feature assignment is already disabled"
            );
        }

        assignment.setStatus("DISABLED");
        assignment.setUnassignedAt(LocalDateTime.now());

        FeatureAssignment saved = assignmentRepository.save(assignment);
        log.info("Assignment disabled successfully for id: {}", id);

        return mapToResponse(saved);
    }

    // DELETE / UNASSIGN

    public void unassignFeature(UUID id) {
        log.info("Unassigning feature assignment with id: {}", id);
        FeatureAssignment assignment = getAssignmentEntity(id);

        assignmentRepository.delete(assignment);
        log.info("Feature assignment unassigned/deleted successfully for id: {}", id);
    }

    // FIND ENTITY

    private FeatureAssignment getAssignmentEntity(UUID id) {

        return assignmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Feature assignment entity not found with id: {}", id);
                    return new FeatureNotFoundException(
                            "Feature assignment not found with id: " + id
                    );
                });
    }

    // STATUS VALIDATION

    private void validateStatus(String status) {

        if (status == null) {
            return;
        }

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

    private FeatureAssignmentResponse mapToResponse(
            FeatureAssignment assignment) {

        FeatureAssignmentResponse response =
                new FeatureAssignmentResponse();

        response.setId(assignment.getId());

        if (assignment.getFeature() != null) {
            response.setFeatureId(
                    assignment.getFeature().getId()
            );
        }

        response.setTenantId(
                assignment.getTenantId()
        );

        response.setOrganizationId(
                assignment.getOrganizationId()
        );

        response.setLicensePlan(
                assignment.getLicensePlan()
        );

        response.setStatus(
                assignment.getStatus()
        );

        response.setConfiguration(
                assignment.getConfiguration()
        );

        response.setAssignedAt(
                assignment.getAssignedAt()
        );

        response.setUnassignedAt(
                assignment.getUnassignedAt()
        );

        response.setCreatedAt(
                assignment.getCreatedAt()
        );

        response.setUpdatedAt(
                assignment.getUpdatedAt()
        );

        response.setCreatedBy(
                assignment.getCreatedBy()
        );

        response.setUpdatedBy(
                assignment.getUpdatedBy()
        );

        return response;
    }
}