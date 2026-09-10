package com.enterprise.superadmin.license_management_service.service;


import com.enterprise.superadmin.license_management_service.dto.request.LicenseAssignmentRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.entity.LicenseAssignment;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.exception.LicenseAlreadyAssignedException;
import com.enterprise.superadmin.license_management_service.exception.LicenseNotFoundException;
import com.enterprise.superadmin.license_management_service.repository.LicenseAssignmentRepository;
import com.enterprise.superadmin.license_management_service.repository.LicenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LicenseAssignmentServiceImpl
        implements LicenseAssignmentService {

    private final LicenseRepository licenseRepository;
    private final LicenseAssignmentRepository assignmentRepository;
    private final LicenseValidationService validationService;

    @Override
    public LicenseResponse assignLicense(
            UUID licenseId,
            LicenseAssignmentRequest request
    ) {
        License license = getActiveLicense(licenseId);

        validationService.validateCanAssign(license);

        validateLicenseNotAlreadyAssigned(licenseId);

        LocalDateTime now = LocalDateTime.now();

        LicenseAssignment assignment = new LicenseAssignment();
        assignment.setLicense(license);
        assignment.setTenantId(request.tenantId());
        assignment.setAssignedBy(request.assignedBy());
        assignment.setAssignedAt(now);
        assignment.setCreatedBy(request.assignedBy());
        assignment.setUpdatedBy(request.assignedBy());

        assignmentRepository.save(assignment);

        /*
         * A successfully assigned PENDING license becomes ACTIVE.
         */
        if (license.getStatus() == LicenseStatus.PENDING) {
            license.setStatus(LicenseStatus.ACTIVE);
            license.setUpdatedBy(request.assignedBy());

            licenseRepository.save(license);
        }

        return mapToResponse(license);
    }

    @Override
    public void revokeLicense(
            UUID licenseId,
            UUID tenantId,
            UUID actorId
    ) {
        /*
         * First verify that the license exists and is not soft-deleted.
         */
        getActiveLicense(licenseId);

        LicenseAssignment assignment =
                assignmentRepository
                        .findByLicenseIdAndTenantIdAndRevokedAtIsNull(
                                licenseId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new LicenseNotFoundException(
                                        "Active license assignment not found for license: "
                                                + licenseId
                                                + " and tenant: "
                                                + tenantId
                                )
                        );

        LocalDateTime now = LocalDateTime.now();

        assignment.setRevokedAt(now);
        assignment.setUpdatedBy(actorId);

        assignmentRepository.save(assignment);
    }

    private License getActiveLicense(UUID licenseId) {
        return licenseRepository
                .findById(licenseId)
                .filter(license -> !license.isDeleted())
                .orElseThrow(() ->
                        new LicenseNotFoundException(
                                "License not found: " + licenseId
                        )
                );
    }

    private void validateLicenseNotAlreadyAssigned(UUID licenseId) {
        boolean alreadyAssigned =
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(licenseId);

        if (alreadyAssigned) {
            throw new LicenseAlreadyAssignedException(
                    "License is already assigned: " + licenseId
            );
        }
    }

    private LicenseResponse mapToResponse(License license) {
        return new LicenseResponse(
                license.getId(),
                license.getLicenseKey(),
                license.getLicensePlan(),
                license.getLicenseType(),
                license.getActivationDate(),
                license.getExpiryDate(),
                license.getStatus(),
                license.getCreatedAt(),
                license.getUpdatedAt()
        );
    }
}

