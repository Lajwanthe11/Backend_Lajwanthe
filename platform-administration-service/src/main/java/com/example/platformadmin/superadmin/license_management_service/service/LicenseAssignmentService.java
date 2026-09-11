package com.example.platformadmin.superadmin.license_management_service.service;



import com.example.platformadmin.superadmin.license_management_service.dto.request.LicenseAssignmentRequest;
import com.example.platformadmin.superadmin.license_management_service.dto.response.LicenseResponse;

import java.util.UUID;

public interface LicenseAssignmentService {

    LicenseResponse assignLicense(
            UUID licenseId,
            LicenseAssignmentRequest request
    );

    void revokeLicense(
            UUID licenseId,
            UUID tenantId,
            UUID actorId
    );
}
