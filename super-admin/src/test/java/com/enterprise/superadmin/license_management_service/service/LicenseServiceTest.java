package com.enterprise.superadmin.license_management_service.service;

import com.enterprise.superadmin.license_management_service.dto.request.LicenseCreateRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseRenewRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseUpdateRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseStatusResponse;
import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.exception.InvalidLicenseStateException;
import com.enterprise.superadmin.license_management_service.exception.LicenseExpiredException;
import com.enterprise.superadmin.license_management_service.exception.LicenseNotFoundException;
import com.enterprise.superadmin.license_management_service.repository.LicenseAssignmentRepository;
import com.enterprise.superadmin.license_management_service.repository.LicenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Mock
    private LicenseAssignmentRepository assignmentRepository;

    @Mock
    private LicenseValidationService validationService;

    @InjectMocks
    private LicenseServiceImpl licenseService;

    private LicenseCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new LicenseCreateRequest(
                "PREMIUM",
                LicenseType.SUBSCRIPTION,
                LocalDate.now(),
                LocalDate.now().plusDays(365)
        );
    }

    @Test
    void shouldCreateLicense() {

        UUID actorId = UUID.randomUUID();

        License savedLicense = createLicense(LicenseStatus.PENDING);

        when(licenseRepository.existsByLicenseKey(any(String.class)))
                .thenReturn(false);

        when(licenseRepository.save(any(License.class)))
                .thenReturn(savedLicense);

        LicenseResponse response =
                licenseService.createLicense(
                        createRequest,
                        actorId
                );

        assertNotNull(response);
        assertEquals(savedLicense.getId(), response.id());
        assertEquals(savedLicense.getLicenseKey(), response.licenseKey());
        assertEquals(LicenseStatus.PENDING, response.status());

        ArgumentCaptor<License> captor =
                ArgumentCaptor.forClass(License.class);

        verify(validationService)
                .validateCreate(captor.capture());

        License createdLicense = captor.getValue();

        assertEquals(
                createRequest.licensePlan(),
                createdLicense.getLicensePlan()
        );

        assertEquals(
                createRequest.licenseType(),
                createdLicense.getLicenseType()
        );

        assertEquals(
                createRequest.activationDate(),
                createdLicense.getActivationDate()
        );

        assertEquals(
                createRequest.expiryDate(),
                createdLicense.getExpiryDate()
        );

        assertEquals(
                actorId,
                createdLicense.getCreatedBy()
        );

        assertEquals(
                actorId,
                createdLicense.getUpdatedBy()
        );

        verify(licenseRepository)
                .save(any(License.class));
    }

    @Test
    void shouldGetLicense() {

        UUID licenseId = UUID.randomUUID();

        License license = createLicense(LicenseStatus.ACTIVE);
        license.setId(licenseId);

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        LicenseResponse response =
                licenseService.getLicense(licenseId);

        assertNotNull(response);
        assertEquals(licenseId, response.id());
        assertEquals(
                license.getLicenseKey(),
                response.licenseKey()
        );
    }

    @Test
    void shouldThrowWhenLicenseDoesNotExist() {

        UUID licenseId = UUID.randomUUID();

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.empty());

        assertThrows(
                LicenseNotFoundException.class,
                () -> licenseService.getLicense(licenseId)
        );
    }

    @Test
    void shouldGetLicenseByKey() {

        String licenseKey = "LIC-TEST123";

        License license = createLicense(LicenseStatus.ACTIVE);
        license.setLicenseKey(licenseKey);

        when(
                licenseRepository
                        .findByLicenseKeyAndDeletedFalse(licenseKey)
        )
                .thenReturn(Optional.of(license));

        LicenseResponse response =
                licenseService.getLicenseByKey(licenseKey);

        assertNotNull(response);
        assertEquals(
                licenseKey,
                response.licenseKey()
        );
    }

    @Test
    void shouldGetAllLicenses() {

        License license =
                createLicense(LicenseStatus.ACTIVE);

        when(licenseRepository.findAll())
                .thenReturn(List.of(license));

        List<LicenseResponse> responses =
                licenseService.getAllLicenses(
                        null,
                        null
                );

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(
                license.getLicenseKey(),
                responses.get(0).licenseKey()
        );
    }

    @Test
    void shouldGetLicensesByPlan() {

        License license =
                createLicense(LicenseStatus.ACTIVE);

        license.setLicensePlan("PREMIUM");

        when(
                licenseRepository
                        .findByLicensePlanAndDeletedFalse("PREMIUM")
        )
                .thenReturn(List.of(license));

        List<LicenseResponse> responses =
                licenseService.getAllLicenses(
                        "PREMIUM",
                        null
                );

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(
                "PREMIUM",
                responses.get(0).licensePlan()
        );
    }

    @Test
    void shouldGetLicensesByStatus() {

        License license =
                createLicense(LicenseStatus.ACTIVE);

        when(
                licenseRepository
                        .findByStatusAndDeletedFalse(
                                LicenseStatus.ACTIVE
                        )
        )
                .thenReturn(List.of(license));

        List<LicenseResponse> responses =
                licenseService.getAllLicenses(
                        null,
                        LicenseStatus.ACTIVE
                );

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(
                LicenseStatus.ACTIVE,
                responses.get(0).status()
        );
    }

    @Test
    void shouldGetLicensesByPlanAndStatus() {

        License license =
                createLicense(LicenseStatus.ACTIVE);

        license.setLicensePlan("PREMIUM");

        when(licenseRepository.findAll())
                .thenReturn(List.of(license));

        List<LicenseResponse> responses =
                licenseService.getAllLicenses(
                        "PREMIUM",
                        LicenseStatus.ACTIVE
                );

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(
                "PREMIUM",
                responses.get(0).licensePlan()
        );
        assertEquals(
                LicenseStatus.ACTIVE,
                responses.get(0).status()
        );
    }

    @Test
    void shouldUpdateLicense() {

        UUID licenseId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license =
                createLicense(LicenseStatus.ACTIVE);

        license.setId(licenseId);

        LicenseUpdateRequest request =
                new LicenseUpdateRequest(
                        "STANDARD",
                        LicenseType.SUBSCRIPTION,
                        LocalDate.now(),
                        LocalDate.now().plusDays(180)
                );

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(licenseRepository.save(license))
                .thenReturn(license);

        LicenseResponse response =
                licenseService.updateLicense(
                        licenseId,
                        request,
                        actorId
                );

        assertNotNull(response);

        assertEquals(
                "STANDARD",
                response.licensePlan()
        );

        assertEquals(
                LicenseType.SUBSCRIPTION,
                response.licenseType()
        );

        assertEquals(
                request.activationDate(),
                response.activationDate()
        );

        assertEquals(
                request.expiryDate(),
                response.expiryDate()
        );

        verify(validationService)
                .validateDates(
                        request.activationDate(),
                        request.expiryDate()
                );

        verify(licenseRepository)
                .save(license);
    }

    @Test
    void shouldRejectUpdateOfExpiredLicense() {

        UUID licenseId = UUID.randomUUID();

        License license =
                createLicense(LicenseStatus.EXPIRED);

        license.setId(licenseId);

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        LicenseUpdateRequest request =
                new LicenseUpdateRequest(
                        "STANDARD",
                        LicenseType.SUBSCRIPTION,
                        LocalDate.now(),
                        LocalDate.now().plusDays(180)
                );

        assertThrows(
                InvalidLicenseStateException.class,
                () -> licenseService.updateLicense(
                        licenseId,
                        request,
                        UUID.randomUUID()
                )
        );

        verify(licenseRepository, never())
                .save(any(License.class));
    }

    @Test
    void shouldActivateLicense() {

        UUID licenseId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license =
                createLicense(LicenseStatus.PENDING);

        license.setId(licenseId);

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(licenseRepository.save(license))
                .thenReturn(license);

        LicenseResponse response =
                licenseService.activateLicense(
                        licenseId,
                        actorId
                );

        assertNotNull(response);
        assertEquals(
                LicenseStatus.ACTIVE,
                response.status()
        );

        verify(validationService)
                .validateTransition(
                        LicenseStatus.PENDING,
                        LicenseStatus.ACTIVE
                );

        verify(licenseRepository)
                .save(license);

        assertEquals(
                actorId,
                license.getUpdatedBy()
        );
    }

    @Test
    void shouldRejectActivationWhenLicenseExpired() {

        UUID licenseId = UUID.randomUUID();

        License license =
                createLicense(LicenseStatus.PENDING);

        license.setId(licenseId);

        license.setExpiryDate(
                LocalDate.now().minusDays(1)
        );

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        assertThrows(
                LicenseExpiredException.class,
                () -> licenseService.activateLicense(
                        licenseId,
                        UUID.randomUUID()
                )
        );

        verify(licenseRepository, never())
                .save(any(License.class));
    }

    @Test
    void shouldSuspendLicense() {

        UUID licenseId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license =
                createLicense(LicenseStatus.ACTIVE);

        license.setId(licenseId);

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(licenseRepository.save(license))
                .thenReturn(license);

        LicenseResponse response =
                licenseService.suspendLicense(
                        licenseId,
                        actorId
                );

        assertNotNull(response);

        assertEquals(
                LicenseStatus.SUSPENDED,
                response.status()
        );

        verify(validationService)
                .validateTransition(
                        LicenseStatus.ACTIVE,
                        LicenseStatus.SUSPENDED
                );

        verify(licenseRepository)
                .save(license);
    }

    @Test
    void shouldRenewLicense() {

        UUID licenseId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        License license =
                createLicense(LicenseStatus.ACTIVE);

        license.setId(licenseId);

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        when(licenseRepository.save(license))
                .thenReturn(license);

        LocalDate newExpiry =
                license.getExpiryDate().plusDays(90);

        LicenseRenewRequest request =
                new LicenseRenewRequest(newExpiry);

        LicenseResponse response =
                licenseService.renewLicense(
                        licenseId,
                        request,
                        actorId
                );

        assertNotNull(response);

        assertEquals(
                newExpiry,
                response.expiryDate()
        );

        verify(validationService)
                .validateRenewal(
                        license,
                        newExpiry
                );

        verify(licenseRepository)
                .save(license);

        assertEquals(
                actorId,
                license.getUpdatedBy()
        );
    }

    @Test
    void shouldGetLicenseStatus() {

        UUID licenseId = UUID.randomUUID();

        License license =
                createLicense(LicenseStatus.ACTIVE);

        license.setId(licenseId);

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        LicenseStatusResponse response =
                licenseService.getLicenseStatus(licenseId);

        assertNotNull(response);

        assertEquals(
                licenseId,
                response.licenseId()
        );

        assertEquals(
                license.getLicenseKey(),
                response.licenseKey()
        );

        assertEquals(
                LicenseStatus.ACTIVE,
                response.status()
        );

        assertFalse(response.expired());
    }

    @Test
    void shouldMarkExpiredLicenses() {

        License license =
                createLicense(LicenseStatus.ACTIVE);

        when(
                licenseRepository
                        .findByExpiryDateBeforeAndStatusNotAndDeletedFalse(
                                any(LocalDate.class),
                                eq(LicenseStatus.EXPIRED)
                        )
        )
                .thenReturn(List.of(license));

        licenseService.markExpiredLicenses();

        assertEquals(
                LicenseStatus.EXPIRED,
                license.getStatus()
        );

        verify(licenseRepository)
                .save(license);
    }

    private License createLicense(
            LicenseStatus status
    ) {

        License license = new License();

        license.setId(UUID.randomUUID());
        license.setLicenseKey("LIC-TEST123");
        license.setLicensePlan("PREMIUM");

        // SOFTWARE does not exist in LicenseType.
        // SUBSCRIPTION is a valid enum value.
        license.setLicenseType(
                LicenseType.SUBSCRIPTION
        );

        license.setActivationDate(
                LocalDate.now()
        );

        license.setExpiryDate(
                LocalDate.now().plusDays(365)
        );

        license.setStatus(status);

        return license;
    }
}