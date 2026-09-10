package com.enterprise.superadmin.license_management_service.repository;



import com.enterprise.superadmin.license_management_service.entity.LicenseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseAssignmentRepository
        extends JpaRepository<LicenseAssignment, UUID> {

    Optional<LicenseAssignment> findByLicenseIdAndTenantIdAndRevokedAtIsNull(
            UUID licenseId,
            UUID tenantId
    );

    Optional<LicenseAssignment> findByLicenseIdAndRevokedAtIsNull(
            UUID licenseId
    );

    List<LicenseAssignment> findByTenantIdAndRevokedAtIsNull(
            UUID tenantId
    );

    boolean existsByLicenseIdAndRevokedAtIsNull(UUID licenseId);

    boolean existsByLicenseIdAndTenantIdAndRevokedAtIsNull(
            UUID licenseId,
            UUID tenantId
    );
}