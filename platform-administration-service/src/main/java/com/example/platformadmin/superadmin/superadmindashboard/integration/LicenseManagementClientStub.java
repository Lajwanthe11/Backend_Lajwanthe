package com.example.platformadmin.superadmin.superadmindashboard.integration;

import com.example.platformadmin.superadmin.license_management_service.enums.LicenseStatus;
import com.example.platformadmin.superadmin.license_management_service.repository.LicenseRepository;
import org.springframework.stereotype.Component;

@Component("superAdminLicenseClientStub")
public class LicenseManagementClientStub implements LicenseManagementClient {

    private final LicenseRepository licenseRepository;

    public LicenseManagementClientStub(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    @Override
    public LicenseStatistics getLicenseStatistics() {
        long activeLicenses = licenseRepository.findByStatusAndDeletedFalse(LicenseStatus.ACTIVE).size();
        long totalSubscriptions = licenseRepository.count();

        return new LicenseStatistics(activeLicenses, totalSubscriptions);
    }
}