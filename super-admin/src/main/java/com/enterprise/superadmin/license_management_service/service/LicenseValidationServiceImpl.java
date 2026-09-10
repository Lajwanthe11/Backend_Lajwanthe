package com.enterprise.superadmin.license_management_service.service;



import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.exception.InvalidLicenseStateException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LicenseValidationServiceImpl
        implements LicenseValidationService {

    @Override
    public void validateDates(
            LocalDate activationDate,
            LocalDate expiryDate
    ) {

        if (activationDate == null) {
            throw new InvalidLicenseStateException(
                    "Activation date is required"
            );
        }

        if (expiryDate == null) {
            throw new InvalidLicenseStateException(
                    "Expiry date is required"
            );
        }

        if (!expiryDate.isAfter(activationDate)) {
            throw new InvalidLicenseStateException(
                    "Expiry date must be later than activation date"
            );
        }
    }

    @Override
    public void validateCreate(License license) {

        validateDates(
                license.getActivationDate(),
                license.getExpiryDate()
        );

        if (license.getLicensePlan() == null ||
                license.getLicensePlan().isBlank()) {

            throw new InvalidLicenseStateException(
                    "License plan is required"
            );
        }

        if (license.getLicenseType() == null) {

            throw new InvalidLicenseStateException(
                    "License type is required"
            );
        }
    }

    @Override
    public void validateTransition(
            LicenseStatus currentStatus,
            LicenseStatus newStatus
    ) {

        boolean valid = switch (currentStatus) {

            case PENDING ->
                    newStatus == LicenseStatus.ACTIVE;

            case ACTIVE ->
                    newStatus == LicenseStatus.SUSPENDED ||
                            newStatus == LicenseStatus.EXPIRED;

            case SUSPENDED ->
                    newStatus == LicenseStatus.ACTIVE ||
                            newStatus == LicenseStatus.EXPIRED;

            case EXPIRED ->
                    false;
        };

        if (!valid) {

            throw new InvalidLicenseStateException(
                    "Invalid license status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }

    @Override
    public void validateRenewal(
            License license,
            LocalDate newExpiryDate
    ) {

        if (license.getStatus() == LicenseStatus.EXPIRED) {
            throw new InvalidLicenseStateException(
                    "Expired license cannot be renewed"
            );
        }

        if (newExpiryDate == null) {
            throw new InvalidLicenseStateException(
                    "New expiry date is required"
            );
        }

        if (!newExpiryDate.isAfter(
                license.getExpiryDate()
        )) {

            throw new InvalidLicenseStateException(
                    "New expiry date must be later than current expiry date"
            );
        }
    }

    @Override
    public void validateCanAssign(License license) {

        if (license.getStatus() != LicenseStatus.ACTIVE &&
                license.getStatus() != LicenseStatus.PENDING) {

            throw new InvalidLicenseStateException(
                    "Only active or pending licenses can be assigned"
            );
        }

        if (license.getExpiryDate().isBefore(LocalDate.now())) {

            throw new InvalidLicenseStateException(
                    "Expired license cannot be assigned"
            );
        }
    }
}
