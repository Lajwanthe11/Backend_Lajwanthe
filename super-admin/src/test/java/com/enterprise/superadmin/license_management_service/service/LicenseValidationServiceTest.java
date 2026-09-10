package com.enterprise.superadmin.license_management_service.service;

import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.exception.InvalidLicenseStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LicenseValidationServiceTest {

    private LicenseValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService =
                new LicenseValidationServiceImpl();
    }

    @Test
    void shouldValidateValidDates() {

        LocalDate activationDate =
                LocalDate.now();

        LocalDate expiryDate =
                activationDate.plusDays(30);

        assertDoesNotThrow(() ->
                validationService.validateDates(
                        activationDate,
                        expiryDate
                )
        );
    }

    @Test
    void shouldRejectNullActivationDate() {

        LocalDate expiryDate =
                LocalDate.now().plusDays(30);

        InvalidLicenseStateException exception =
                assertThrows(
                        InvalidLicenseStateException.class,
                        () -> validationService.validateDates(
                                null,
                                expiryDate
                        )
                );

        assertEquals(
                "Activation date is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNullExpiryDate() {

        LocalDate activationDate =
                LocalDate.now();

        InvalidLicenseStateException exception =
                assertThrows(
                        InvalidLicenseStateException.class,
                        () -> validationService.validateDates(
                                activationDate,
                                null
                        )
                );

        assertEquals(
                "Expiry date is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectExpiryBeforeActivation() {

        LocalDate activationDate =
                LocalDate.now().plusDays(10);

        LocalDate expiryDate =
                LocalDate.now();

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateDates(
                        activationDate,
                        expiryDate
                )
        );
    }

    @Test
    void shouldRejectExpiryEqualToActivation() {

        LocalDate date =
                LocalDate.now();

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateDates(
                        date,
                        date
                )
        );
    }

    @Test
    void shouldValidateCreateLicense() {

        License license = createValidLicense();

        assertDoesNotThrow(() ->
                validationService.validateCreate(license)
        );
    }

    @Test
    void shouldRejectCreateWhenLicensePlanIsMissing() {

        License license = createValidLicense();
        license.setLicensePlan(null);

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateCreate(license)
        );
    }

    @Test
    void shouldRejectCreateWhenLicensePlanIsBlank() {

        License license = createValidLicense();
        license.setLicensePlan("   ");

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateCreate(license)
        );
    }

    @Test
    void shouldRejectCreateWhenLicenseTypeIsMissing() {

        License license = createValidLicense();
        license.setLicenseType(null);

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateCreate(license)
        );
    }

    @Test
    void shouldAllowPendingToActive() {

        assertDoesNotThrow(() ->
                validationService.validateTransition(
                        LicenseStatus.PENDING,
                        LicenseStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldAllowActiveToSuspended() {

        assertDoesNotThrow(() ->
                validationService.validateTransition(
                        LicenseStatus.ACTIVE,
                        LicenseStatus.SUSPENDED
                )
        );
    }

    @Test
    void shouldAllowActiveToExpired() {

        assertDoesNotThrow(() ->
                validationService.validateTransition(
                        LicenseStatus.ACTIVE,
                        LicenseStatus.EXPIRED
                )
        );
    }

    @Test
    void shouldAllowSuspendedToActive() {

        assertDoesNotThrow(() ->
                validationService.validateTransition(
                        LicenseStatus.SUSPENDED,
                        LicenseStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldAllowSuspendedToExpired() {

        assertDoesNotThrow(() ->
                validationService.validateTransition(
                        LicenseStatus.SUSPENDED,
                        LicenseStatus.EXPIRED
                )
        );
    }

    @Test
    void shouldRejectPendingToSuspended() {

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateTransition(
                        LicenseStatus.PENDING,
                        LicenseStatus.SUSPENDED
                )
        );
    }

    @Test
    void shouldRejectActiveToPending() {

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateTransition(
                        LicenseStatus.ACTIVE,
                        LicenseStatus.PENDING
                )
        );
    }

    @Test
    void shouldRejectExpiredToActive() {

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateTransition(
                        LicenseStatus.EXPIRED,
                        LicenseStatus.ACTIVE
                )
        );
    }

    @Test
    void shouldValidateRenewal() {

        License license = createValidLicense();

        LocalDate newExpiryDate =
                license.getExpiryDate().plusDays(30);

        assertDoesNotThrow(() ->
                validationService.validateRenewal(
                        license,
                        newExpiryDate
                )
        );
    }

    @Test
    void shouldRejectRenewalForExpiredLicense() {

        License license = createValidLicense();
        license.setStatus(LicenseStatus.EXPIRED);

        LocalDate newExpiryDate =
                license.getExpiryDate().plusDays(30);

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateRenewal(
                        license,
                        newExpiryDate
                )
        );
    }

    @Test
    void shouldRejectRenewalWithNullExpiryDate() {

        License license = createValidLicense();

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateRenewal(
                        license,
                        null
                )
        );
    }

    @Test
    void shouldRejectRenewalWithEarlierExpiry() {

        License license = createValidLicense();

        LocalDate newExpiryDate =
                license.getExpiryDate().minusDays(1);

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateRenewal(
                        license,
                        newExpiryDate
                )
        );
    }

    @Test
    void shouldRejectRenewalWithSameExpiry() {

        License license = createValidLicense();

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateRenewal(
                        license,
                        license.getExpiryDate()
                )
        );
    }

    @Test
    void shouldAllowActiveLicenseAssignment() {

        License license =
                createValidLicense();

        license.setStatus(LicenseStatus.ACTIVE);

        assertDoesNotThrow(() ->
                validationService.validateCanAssign(license)
        );
    }

    @Test
    void shouldAllowPendingLicenseAssignment() {

        License license =
                createValidLicense();

        license.setStatus(LicenseStatus.PENDING);

        assertDoesNotThrow(() ->
                validationService.validateCanAssign(license)
        );
    }

    @Test
    void shouldRejectSuspendedLicenseAssignment() {

        License license =
                createValidLicense();

        license.setStatus(LicenseStatus.SUSPENDED);

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateCanAssign(license)
        );
    }

    @Test
    void shouldRejectExpiredLicenseAssignment() {

        License license =
                createValidLicense();

        license.setStatus(LicenseStatus.ACTIVE);
        license.setExpiryDate(
                LocalDate.now().minusDays(1)
        );

        assertThrows(
                InvalidLicenseStateException.class,
                () -> validationService.validateCanAssign(license)
        );
    }

    private License createValidLicense() {

        License license = new License();

        license.setId(UUID.randomUUID());
        license.setLicenseKey("LIC-TEST123");
        license.setLicensePlan("PREMIUM");
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
                LicenseStatus.PENDING
        );

        return license;
    }
}