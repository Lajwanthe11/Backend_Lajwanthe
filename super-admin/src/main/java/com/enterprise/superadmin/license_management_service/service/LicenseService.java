package com.enterprise.superadmin.license_management_service.service;




import com.enterprise.superadmin.license_management_service.dto.request.LicenseCreateRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseRenewRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseUpdateRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseStatusResponse;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;

import java.util.List;
import java.util.UUID;

public interface LicenseService {

    LicenseResponse createLicense(
            LicenseCreateRequest request,
            UUID actorId
    );

    LicenseResponse getLicense(UUID licenseId);

    LicenseResponse getLicenseByKey(String licenseKey);

    List<LicenseResponse> getAllLicenses(
            String plan,
            LicenseStatus status
    );

    LicenseResponse updateLicense(
            UUID licenseId,
            LicenseUpdateRequest request,
            UUID actorId
    );

    LicenseResponse activateLicense(
            UUID licenseId,
            UUID actorId
    );

    LicenseResponse reactivateLicense(
            UUID licenseId,
            UUID actorId
    );

    LicenseResponse suspendLicense(
            UUID licenseId,
            UUID actorId
    );

    LicenseResponse renewLicense(
            UUID licenseId,
            LicenseRenewRequest request,
            UUID actorId
    );

    LicenseStatusResponse getLicenseStatus(
            UUID licenseId
    );

    void markExpiredLicenses();
}