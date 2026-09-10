package com.enterprise.superadmin.license_management_service.controller;

import com.enterprise.superadmin.license_management_service.dto.request.LicenseAssignmentRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.service.LicenseAssignmentService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LicenseAssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class LicenseAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LicenseAssignmentService assignmentService;


    @Test
    void shouldAssignLicense()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID assignedBy =
                UUID.randomUUID();

        LicenseResponse response =
                new LicenseResponse(
                        licenseId,
                        "LIC-TEST123",
                        "PREMIUM",
                        LicenseType.SUBSCRIPTION,
                        LocalDate.now(),
                        LocalDate.now().plusDays(365),
                        LicenseStatus.ACTIVE,
                        null,
                        null
                );

        when(
                assignmentService.assignLicense(
                        eq(licenseId),
                        any(LicenseAssignmentRequest.class)
                )
        ).thenReturn(response);

        String requestBody = """
                {
                    "tenantId": "%s",
                    "assignedBy": "%s"
                }
                """.formatted(
                tenantId,
                assignedBy
        );

        mockMvc.perform(
                        post(
                                "/api/v1/licenses/{licenseId}/assign",
                                licenseId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                );

        verify(
                assignmentService
        ).assignLicense(
                eq(licenseId),
                any(LicenseAssignmentRequest.class)
        );
    }


    @Test
    void shouldReturnBadRequestWhenTenantIdIsMissing()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID assignedBy =
                UUID.randomUUID();

        String requestBody = """
                {
                    "assignedBy": "%s"
                }
                """.formatted(assignedBy);

        mockMvc.perform(
                        post(
                                "/api/v1/licenses/{licenseId}/assign",
                                licenseId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                assignmentService
        );
    }


    @Test
    void shouldReturnBadRequestWhenAssignedByIsMissing()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        String requestBody = """
                {
                    "tenantId": "%s"
                }
                """.formatted(tenantId);

        mockMvc.perform(
                        post(
                                "/api/v1/licenses/{licenseId}/assign",
                                licenseId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                assignmentService
        );
    }


    @Test
    void shouldReturnBadRequestWhenBothAssignmentFieldsAreMissing()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/v1/licenses/{licenseId}/assign",
                                licenseId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                assignmentService
        );
    }


    @Test
    void shouldReturnBadRequestWhenLicenseIdIsInvalid()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        UUID assignedBy =
                UUID.randomUUID();

        String requestBody = """
                {
                    "tenantId": "%s",
                    "assignedBy": "%s"
                }
                """.formatted(
                tenantId,
                assignedBy
        );

        mockMvc.perform(
                        post(
                                "/api/v1/licenses/{licenseId}/assign",
                                "invalid-uuid"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                assignmentService
        );
    }


    @Test
    void shouldRevokeLicenseAssignmentWithoutActorId()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        doNothing()
                .when(assignmentService)
                .revokeLicense(
                        licenseId,
                        tenantId,
                        null
                );

        mockMvc.perform(
                        delete(
                                "/api/v1/licenses/{licenseId}/assign/{tenantId}",
                                licenseId,
                                tenantId
                        )
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(
                assignmentService
        ).revokeLicense(
                licenseId,
                tenantId,
                null
        );
    }


    @Test
    void shouldRevokeLicenseAssignmentWithActorId()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID actorId =
                UUID.randomUUID();

        doNothing()
                .when(assignmentService)
                .revokeLicense(
                        licenseId,
                        tenantId,
                        actorId
                );

        mockMvc.perform(
                        delete(
                                "/api/v1/licenses/{licenseId}/assign/{tenantId}",
                                licenseId,
                                tenantId
                        )
                                .header(
                                        "X-Actor-Id",
                                        actorId.toString()
                                )
                )
                .andExpect(
                        status().isNoContent()
                );

        verify(
                assignmentService
        ).revokeLicense(
                licenseId,
                tenantId,
                actorId
        );
    }


    @Test
    void shouldReturnBadRequestWhenRevokeLicenseIdIsInvalid()
            throws Exception {

        UUID tenantId =
                UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/api/v1/licenses/{licenseId}/assign/{tenantId}",
                                "invalid-license-id",
                                tenantId
                        )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                assignmentService
        );
    }


    @Test
    void shouldReturnBadRequestWhenRevokeTenantIdIsInvalid()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/api/v1/licenses/{licenseId}/assign/{tenantId}",
                                licenseId,
                                "invalid-tenant-id"
                        )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                assignmentService
        );
    }


    @Test
    void shouldReturnBadRequestWhenActorIdIsInvalid()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/api/v1/licenses/{licenseId}/assign/{tenantId}",
                                licenseId,
                                tenantId
                        )
                                .header(
                                        "X-Actor-Id",
                                        "invalid-uuid"
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(
                assignmentService
        );
    }
}