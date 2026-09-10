package com.enterprise.superadmin.license_management_service.repository;

import com.enterprise.superadmin.license_management_service.entity.LicenseAssignment;
import com.enterprise.superadmin.license_management_service.repository.LicenseAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseAssignmentRepositoryTest {

    @Mock
    private LicenseAssignmentRepository assignmentRepository;

    @Test
    void shouldFindActiveAssignmentByLicenseIdAndTenantId() {

        UUID licenseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        LicenseAssignment assignment =
                new LicenseAssignment();

        assignment.setTenantId(tenantId);

        when(
                assignmentRepository
                        .findByLicenseIdAndTenantIdAndRevokedAtIsNull(
                                licenseId,
                                tenantId
                        )
        )
                .thenReturn(
                        Optional.of(assignment)
                );

        Optional<LicenseAssignment> result =
                assignmentRepository
                        .findByLicenseIdAndTenantIdAndRevokedAtIsNull(
                                licenseId,
                                tenantId
                        );

        assertTrue(result.isPresent());

        assertEquals(
                tenantId,
                result.get().getTenantId()
        );

        verify(assignmentRepository)
                .findByLicenseIdAndTenantIdAndRevokedAtIsNull(
                        licenseId,
                        tenantId
                );
    }

    @Test
    void shouldFindActiveAssignmentByLicenseId() {

        UUID licenseId = UUID.randomUUID();

        LicenseAssignment assignment =
                new LicenseAssignment();

        when(
                assignmentRepository
                        .findByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        )
        )
                .thenReturn(
                        Optional.of(assignment)
                );

        Optional<LicenseAssignment> result =
                assignmentRepository
                        .findByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        );

        assertTrue(result.isPresent());

        verify(assignmentRepository)
                .findByLicenseIdAndRevokedAtIsNull(
                        licenseId
                );
    }

    @Test
    void shouldFindActiveAssignmentsByTenantId() {

        UUID tenantId = UUID.randomUUID();

        LicenseAssignment assignment =
                new LicenseAssignment();

        assignment.setTenantId(tenantId);

        when(
                assignmentRepository
                        .findByTenantIdAndRevokedAtIsNull(
                                tenantId
                        )
        )
                .thenReturn(
                        List.of(assignment)
                );

        List<LicenseAssignment> result =
                assignmentRepository
                        .findByTenantIdAndRevokedAtIsNull(
                                tenantId
                        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                tenantId,
                result.get(0).getTenantId()
        );

        verify(assignmentRepository)
                .findByTenantIdAndRevokedAtIsNull(
                        tenantId
                );
    }

    @Test
    void shouldCheckActiveAssignmentByLicenseId() {

        UUID licenseId = UUID.randomUUID();

        when(
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        )
        )
                .thenReturn(true);

        boolean result =
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        );

        assertTrue(result);

        verify(assignmentRepository)
                .existsByLicenseIdAndRevokedAtIsNull(
                        licenseId
                );
    }

    @Test
    void shouldCheckActiveAssignmentByLicenseIdAndTenantId() {

        UUID licenseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        when(
                assignmentRepository
                        .existsByLicenseIdAndTenantIdAndRevokedAtIsNull(
                                licenseId,
                                tenantId
                        )
        )
                .thenReturn(true);

        boolean result =
                assignmentRepository
                        .existsByLicenseIdAndTenantIdAndRevokedAtIsNull(
                                licenseId,
                                tenantId
                        );

        assertTrue(result);

        verify(assignmentRepository)
                .existsByLicenseIdAndTenantIdAndRevokedAtIsNull(
                        licenseId,
                        tenantId
                );
    }

    @Test
    void shouldReturnFalseWhenNoActiveAssignmentExists() {

        UUID licenseId = UUID.randomUUID();

        when(
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        )
        )
                .thenReturn(false);

        boolean result =
                assignmentRepository
                        .existsByLicenseIdAndRevokedAtIsNull(
                                licenseId
                        );

        assertFalse(result);

        verify(assignmentRepository)
                .existsByLicenseIdAndRevokedAtIsNull(
                        licenseId
                );
    }
}