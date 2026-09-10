package com.enterprise.superadmin.license_management_service.repository;

import com.enterprise.superadmin.license_management_service.entity.License;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.repository.LicenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseRepositoryTest {

    @Mock
    private LicenseRepository licenseRepository;

    @Test
    void shouldFindLicenseById() {

        UUID licenseId = UUID.randomUUID();

        License license = createLicense(
                licenseId,
                LicenseStatus.ACTIVE
        );

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.of(license));

        Optional<License> result =
                licenseRepository.findById(licenseId);

        assertTrue(result.isPresent());

        assertEquals(
                licenseId,
                result.get().getId()
        );

        assertEquals(
                "LIC-TEST123",
                result.get().getLicenseKey()
        );

        verify(licenseRepository)
                .findById(licenseId);
    }

    @Test
    void shouldReturnEmptyWhenLicenseDoesNotExist() {

        UUID licenseId = UUID.randomUUID();

        when(licenseRepository.findById(licenseId))
                .thenReturn(Optional.empty());

        Optional<License> result =
                licenseRepository.findById(licenseId);

        assertFalse(result.isPresent());

        verify(licenseRepository)
                .findById(licenseId);
    }

    @Test
    void shouldFindLicenseByLicenseKey() {

        String licenseKey = "LIC-TEST123";

        License license = createLicense(
                UUID.randomUUID(),
                LicenseStatus.ACTIVE
        );

        license.setLicenseKey(licenseKey);

        when(
                licenseRepository
                        .findByLicenseKeyAndDeletedFalse(
                                licenseKey
                        )
        )
                .thenReturn(Optional.of(license));

        Optional<License> result =
                licenseRepository
                        .findByLicenseKeyAndDeletedFalse(
                                licenseKey
                        );

        assertTrue(result.isPresent());

        assertEquals(
                licenseKey,
                result.get().getLicenseKey()
        );

        verify(licenseRepository)
                .findByLicenseKeyAndDeletedFalse(
                        licenseKey
                );
    }

    @Test
    void shouldCheckLicenseKeyExists() {

        String licenseKey = "LIC-TEST123";

        when(
                licenseRepository
                        .existsByLicenseKey(licenseKey)
        )
                .thenReturn(true);

        boolean result =
                licenseRepository
                        .existsByLicenseKey(licenseKey);

        assertTrue(result);

        verify(licenseRepository)
                .existsByLicenseKey(licenseKey);
    }

    @Test
    void shouldReturnLicensesByStatus() {

        License license = createLicense(
                UUID.randomUUID(),
                LicenseStatus.ACTIVE
        );

        when(
                licenseRepository
                        .findByStatusAndDeletedFalse(
                                LicenseStatus.ACTIVE
                        )
        )
                .thenReturn(List.of(license));

        List<License> result =
                licenseRepository
                        .findByStatusAndDeletedFalse(
                                LicenseStatus.ACTIVE
                        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                LicenseStatus.ACTIVE,
                result.get(0).getStatus()
        );

        verify(licenseRepository)
                .findByStatusAndDeletedFalse(
                        LicenseStatus.ACTIVE
                );
    }

    @Test
    void shouldReturnLicensesByPlan() {

        License license = createLicense(
                UUID.randomUUID(),
                LicenseStatus.ACTIVE
        );

        license.setLicensePlan("PREMIUM");

        when(
                licenseRepository
                        .findByLicensePlanAndDeletedFalse(
                                "PREMIUM"
                        )
        )
                .thenReturn(List.of(license));

        List<License> result =
                licenseRepository
                        .findByLicensePlanAndDeletedFalse(
                                "PREMIUM"
                        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                "PREMIUM",
                result.get(0).getLicensePlan()
        );

        verify(licenseRepository)
                .findByLicensePlanAndDeletedFalse(
                        "PREMIUM"
                );
    }

    @Test
    void shouldFindLicensesExpiringBeforeDate() {

        LocalDate date = LocalDate.now();

        License license = createLicense(
                UUID.randomUUID(),
                LicenseStatus.ACTIVE
        );

        license.setExpiryDate(
                date.minusDays(1)
        );

        when(
                licenseRepository
                        .findByExpiryDateBeforeAndStatusNotAndDeletedFalse(
                                date,
                                LicenseStatus.EXPIRED
                        )
        )
                .thenReturn(List.of(license));

        List<License> result =
                licenseRepository
                        .findByExpiryDateBeforeAndStatusNotAndDeletedFalse(
                                date,
                                LicenseStatus.EXPIRED
                        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                LicenseStatus.ACTIVE,
                result.get(0).getStatus()
        );

        assertTrue(
                result.get(0)
                        .getExpiryDate()
                        .isBefore(date)
        );

        verify(licenseRepository)
                .findByExpiryDateBeforeAndStatusNotAndDeletedFalse(
                        date,
                        LicenseStatus.EXPIRED
                );
    }

    private License createLicense(
            UUID licenseId,
            LicenseStatus status
    ) {

        License license = new License();

        license.setId(licenseId);

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

        license.setStatus(status);

        return license;
    }
}