package com.enterprise.superadmin.license_management_service.service;




import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;

import java.time.LocalDate;

public interface LicenseValidationService {

    void validateDates(
            LocalDate activationDate,
            LocalDate expiryDate
    );

    void validateCreate(License license);

    void validateTransition(
            LicenseStatus currentStatus,
            LicenseStatus newStatus
    );

    void validateRenewal(
            License license,
            LocalDate newExpiryDate
    );

    void validateCanAssign(License license);
}
