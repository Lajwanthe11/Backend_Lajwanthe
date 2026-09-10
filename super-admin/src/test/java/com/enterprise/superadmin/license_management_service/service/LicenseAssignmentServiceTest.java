package com.enterprise.superadmin.license_management_service.service;

import com.enterprise.superadmin.license_management_service.dto.request.LicenseAssignmentRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.entity.LicenseAssignment;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.exception.LicenseAlreadyAssignedException;
import com.enterprise.superadmin.license_management_service.exception.LicenseNotFoundException;
import com.enterprise.superadmin.license_management_service.repository.LicenseAssignmentRepository;
import com.enterprise.superadmin.license_management_service.repository.LicenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseAssignmentServiceTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private LicenseAssignmentRepository assignmentRepository;

    @Mock
    private LicenseValidationService validationService;

    @InjectMocks
    private LicenseAssignmentServiceImpl assignmentService;

    @Test
    void shouldAssignLicense() {

        UUID licenseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license = createActiveLicense();

        LicenseAssignmentRequest request =
                new LicenseAssignmentRequest(
                        tenantId,
                        actorId
                );

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        )
        )
                .thenReturn(false);

        LicenseResponse response =
                assignmentService.assignLicense(
                        licenseId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                license.getId(),
                response.id()
        );

        assertEquals(
                license.getLicenseKey(),
                response.licenseKey()
        );

        assertEquals(
                LicenseStatus.ACTIVE,
                response.status()
        );

        verify(validationService)
                .validateCanAssign(license);

        verify(assignmentRepository)
                .save(any(LicenseAssignment.class));

        verify(
                assignmentRepository
        )
                .existsByLicenseIdAndRevokedAtIsNull(
                        licenseId
                );
    }

    @Test
    void shouldAssignPendingLicenseAndActivateIt() {

        UUID licenseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license = createPendingLicense();

        LicenseAssignmentRequest request =
                new LicenseAssignmentRequest(
                        tenantId,
                        actorId
                );

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        )
        )
                .thenReturn(false);

        when(
                licenseRepository.save(license)
        )
                .thenReturn(license);

        LicenseResponse response =
                assignmentService.assignLicense(
                        licenseId,
                        request
                );

        assertNotNull(response);

        assertEquals(
                LicenseStatus.ACTIVE,
                license.getStatus()
        );

        assertEquals(
                LicenseStatus.ACTIVE,
                response.status()
        );

        verify(validationService)
                .validateCanAssign(license);

        verify(assignmentRepository)
                .save(any(LicenseAssignment.class));

        verify(licenseRepository)
                .save(license);
    }

    @Test
    void shouldRejectAlreadyAssignedLicense() {

        UUID licenseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license =
                createActiveLicense();

        LicenseAssignmentRequest request =
                new LicenseAssignmentRequest(
                        tenantId,
                        actorId
                );

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        )
        )
                .thenReturn(true);

        assertThrows(
                LicenseAlreadyAssignedException.class,
                () ->
                        assignmentService.assignLicense(
                                licenseId,
                                request
                        )
        );

        verify(validationService)
                .validateCanAssign(license);

        verify(
                assignmentRepository,
                never()
        )
                .save(any(LicenseAssignment.class));

        verify(
                licenseRepository,
                never()
        )
                .save(any(License.class));
    }

    @Test
    void shouldThrowWhenLicenseDoesNotExist() {

        UUID licenseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        LicenseAssignmentRequest request =
                new LicenseAssignmentRequest(
                        tenantId,
                        actorId
                );

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.empty());

        assertThrows(
                LicenseNotFoundException.class,
                () ->
                        assignmentService.assignLicense(
                                licenseId,
                                request
                        )
        );

        verify(
                validationService,
                never()
        )
                .validateCanAssign(any(License.class));

        verify(
                assignmentRepository,
                never()
        )
                .save(any(LicenseAssignment.class));
    }

    @Test
    void shouldRevokeLicense() {

        UUID licenseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license =
                createActiveLicense();

        LicenseAssignment assignment =
                new LicenseAssignment();

        assignment.setTenantId(tenantId);

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(
                assignmentRepository
                        .findByLicenseIdAndTenantIdAndRevokedAtIsNull(
                                licenseId,
                                tenantId
                        )
        )
                .thenReturn(Optional.of(assignment));

        assignmentService.revokeLicense(
                licenseId,
                tenantId,
                actorId
        );

        assertNotNull(
                assignment.getRevokedAt()
        );

        assertEquals(
                actorId,
                assignment.getUpdatedBy()
        );

        verify(
                assignmentRepository
        )
                .save(assignment);
    }

    @Test
    void shouldRejectRevokeWhenAssignmentDoesNotExist() {

        UUID licenseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license =
                createActiveLicense();

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(
                assignmentRepository
                        .findByLicenseIdAndTenantIdAndRevokedAtIsNull(
                                licenseId,
                                tenantId
                        )
        )
                .thenReturn(Optional.empty());

        assertThrows(
                LicenseNotFoundException.class,
                () ->
                        assignmentService.revokeLicense(
                                licenseId,
                                tenantId,
                                actorId
                        )
        );

        verify(
                assignmentRepository,
                never()
        )
                .save(any(LicenseAssignment.class));
    }

    private License createActiveLicense() {

        License license = new License();

        license.setId(UUID.randomUUID());

        license.setLicenseKey(
                "LIC-TEST123"
        );

        license.setLicensePlan(
                "PREMIUM"
        );

        license.setLicenseType(
                LicenseType.SUBSCRIPTION
        );

        license.setActivationDate(
                LocalDate.now()
        );

        license.setExpiryDate(
                LocalDate.now().plusDays(365)
        );

        license.setStatus(
                LicenseStatus.ACTIVE
        );

        return license;
    }

    private License createPendingLicense() {

        License license =
                createActiveLicense();

        license.setStatus(
                LicenseStatus.PENDING
        );

        return license;
    }
}