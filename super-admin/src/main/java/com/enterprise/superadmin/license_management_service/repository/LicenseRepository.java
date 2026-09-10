package com.enterprise.superadmin.license_management_service.repository;


import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository
        extends JpaRepository<License, UUID>,
        JpaSpecificationExecutor<License> {

    Optional<License> findByLicenseKeyAndDeletedFalse(String licenseKey);

    List<License> findByStatusAndDeletedFalse(LicenseStatus status);

    List<License> findByLicensePlanAndDeletedFalse(String licensePlan);

    List<License> findByExpiryDateBeforeAndStatusNotAndDeletedFalse(
            LocalDate date,
            LicenseStatus status
    );

    boolean existsByLicenseKey(String licenseKey);
}