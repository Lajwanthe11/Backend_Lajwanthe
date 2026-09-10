package com.enterprise.superadmin.license_management_service.service;



import com.enterprise.superadmin.license_management_service.dto.request.LicenseCreateRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseRenewRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseUpdateRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseStatusResponse;
import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.exception.InvalidLicenseStateException;
import com.enterprise.superadmin.license_management_service.exception.LicenseExpiredException;
import com.enterprise.superadmin.license_management_service.exception.LicenseNotFoundException;
import com.enterprise.superadmin.license_management_service.repository.LicenseAssignmentRepository;
import com.enterprise.superadmin.license_management_service.repository.LicenseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LicenseServiceImpl implements LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseAssignmentRepository assignmentRepository;
    private final LicenseValidationService validationService;

    @Override
    public LicenseResponse createLicense(
            LicenseCreateRequest request,
            UUID actorId
    ) {

        License license = new License();

        license.setLicenseKey(generateLicenseKey());
        license.setLicensePlan(request.licensePlan());
        license.setLicenseType(request.licenseType());
        license.setActivationDate(request.activationDate());
        license.setExpiryDate(request.expiryDate());
        license.setStatus(LicenseStatus.PENDING);

        license.setCreatedBy(actorId);
        license.setUpdatedBy(actorId);

        validationService.validateCreate(license);

        License saved = licenseRepository.save(license);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public LicenseResponse getLicense(UUID licenseId) {

        License license = getLicenseEntity(licenseId);

        return mapToResponse(license);
    }

    @Override
    @Transactional
    public LicenseResponse getLicenseByKey(
            String licenseKey
    ) {

        License license = licenseRepository
                .findByLicenseKeyAndDeletedFalse(licenseKey)
                .orElseThrow(() ->
                        new LicenseNotFoundException(
                                "License not found: " + licenseKey
                        )
                );

        return mapToResponse(license);
    }

    @Override
    @Transactional
    public List<LicenseResponse> getAllLicenses(
            String plan,
            LicenseStatus status
    ) {

        List<License> licenses;

        if (plan != null && status != null) {

            licenses = licenseRepository
                    .findAll()
                    .stream()
                    .filter(l ->
                            !l.isDeleted()
                                    && plan.equalsIgnoreCase(
                                    l.getLicensePlan()
                            )
                                    && status == l.getStatus()
                    )
                    .toList();

        } else if (plan != null) {

            licenses = licenseRepository
                    .findByLicensePlanAndDeletedFalse(plan);

        } else if (status != null) {

            licenses = licenseRepository
                    .findByStatusAndDeletedFalse(status);

        } else {

            licenses = licenseRepository
                    .findAll()
                    .stream()
                    .filter(l -> !l.isDeleted())
                    .toList();
        }

        return licenses
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public LicenseResponse updateLicense(
            UUID licenseId,
            LicenseUpdateRequest request,
            UUID actorId
    ) {

        License license = getLicenseEntity(licenseId);

        if (license.getStatus() == LicenseStatus.EXPIRED) {

            throw new InvalidLicenseStateException(
                    "Expired license cannot be edited"
            );
        }

        validationService.validateDates(
                request.activationDate(),
                request.expiryDate()
        );

        license.setLicensePlan(request.licensePlan());
        license.setLicenseType(request.licenseType());
        license.setActivationDate(request.activationDate());
        license.setExpiryDate(request.expiryDate());
        license.setUpdatedBy(actorId);

        return mapToResponse(
                licenseRepository.save(license)
        );
    }

    @Override
    public LicenseResponse activateLicense(
            UUID licenseId,
            UUID actorId
    ) {

        License license = getLicenseEntity(licenseId);

        validationService.validateTransition(
                license.getStatus(),
                LicenseStatus.ACTIVE
        );

        if (license.getExpiryDate().isBefore(LocalDate.now())) {

            throw new LicenseExpiredException(
                    "License has already expired"
            );
        }

        license.setStatus(LicenseStatus.ACTIVE);
        license.setUpdatedBy(actorId);

        return mapToResponse(licenseRepository.save(license));
    }

    @Override
    public LicenseResponse reactivateLicense(
            UUID licenseId,
            UUID actorId
    ) {

        License license = getLicenseEntity(licenseId);

        validationService.validateTransition(
                license.getStatus(),
                LicenseStatus.ACTIVE
        );

        if (license.getExpiryDate().isBefore(LocalDate.now())) {

            throw new LicenseExpiredException(
                    "Expired license cannot be reactivated"
            );
        }

        license.setStatus(LicenseStatus.ACTIVE);
        license.setUpdatedBy(actorId);

        return mapToResponse(licenseRepository.save(license));
    }

    @Override
    public LicenseResponse suspendLicense(
            UUID licenseId,
            UUID actorId
    ) {

        License license = getLicenseEntity(licenseId);

        validationService.validateTransition(
                license.getStatus(),
                LicenseStatus.SUSPENDED
        );

        license.setStatus(LicenseStatus.SUSPENDED);
        license.setUpdatedBy(actorId);

        return mapToResponse(licenseRepository.save(license));
    }

    @Override
    public LicenseResponse renewLicense(
            UUID licenseId,
            LicenseRenewRequest request,
            UUID actorId
    ) {

        License license = getLicenseEntity(licenseId);

        validationService.validateRenewal(
                license,
                request.newExpiryDate()
        );

        license.setExpiryDate(request.newExpiryDate());
        license.setUpdatedBy(actorId);

        if (license.getStatus() == LicenseStatus.EXPIRED) {
            license.setStatus(LicenseStatus.ACTIVE);
        }

        return mapToResponse(
                licenseRepository.save(license)
        );
    }

    @Override
    @Transactional
    public LicenseStatusResponse getLicenseStatus(
            UUID licenseId
    ) {

        License license = getLicenseEntity(licenseId);

        boolean expired =
                license.getExpiryDate()
                        .isBefore(LocalDate.now());

        return new LicenseStatusResponse(
                license.getId(),
                license.getLicenseKey(),
                license.getStatus(),
                license.getActivationDate(),
                license.getExpiryDate(),
                expired
        );
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void markExpiredLicenses() {

        List<License> licenses =
                licenseRepository
                        .findByExpiryDateBeforeAndStatusNotAndDeletedFalse(
                                LocalDate.now(),
                                LicenseStatus.EXPIRED
                        );

        for (License license : licenses) {

            license.setStatus(LicenseStatus.EXPIRED);
            license.setUpdatedAt(LocalDateTime.now());

            licenseRepository.save(license);
        }
    }

    private License getLicenseEntity(UUID licenseId) {

        return licenseRepository
                .findById(licenseId)
                .filter(license -> !license.isDeleted())
                .orElseThrow(() ->
                        new LicenseNotFoundException(
                                "License not found: " + licenseId
                        )
                );
    }

    private String generateLicenseKey() {

        String key;

        do {

            key = "LIC-"
                    + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 24)
                    .toUpperCase();

        } while (licenseRepository.existsByLicenseKey(key));

        return key;
    }

    private LicenseResponse mapToResponse(
            License license
    ) {

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