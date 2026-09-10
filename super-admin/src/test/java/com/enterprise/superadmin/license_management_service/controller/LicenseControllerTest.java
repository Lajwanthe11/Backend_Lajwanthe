package com.enterprise.superadmin.license_management_service.controller;

import com.enterprise.superadmin.license_management_service.dto.request.LicenseCreateRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseRenewRequest;
import com.enterprise.superadmin.license_management_service.dto.request.LicenseUpdateRequest;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseResponse;
import com.enterprise.superadmin.license_management_service.dto.response.LicenseStatusResponse;
import com.enterprise.superadmin.license_management_service.enums.LicenseStatus;
import com.enterprise.superadmin.license_management_service.enums.LicenseType;
import com.enterprise.superadmin.license_management_service.service.LicenseService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LicenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class LicenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LicenseService licenseService;


    @Test
    void shouldCreateLicense() throws Exception {

        UUID actorId = UUID.randomUUID();
        UUID licenseId = UUID.randomUUID();

        LicenseResponse response =
                createResponse(
                        licenseId,
                        LicenseStatus.PENDING
                );

        when(
                licenseService.createLicense(
                        any(LicenseCreateRequest.class),
                        eq(actorId)
                )
        ).thenReturn(response);

        String requestBody = """
                {
                    "licensePlan": "PREMIUM",
                    "licenseType": "SUBSCRIPTION",
                    "activationDate": "%s",
                    "expiryDate": "%s"
                }
                """.formatted(
                LocalDate.now(),
                LocalDate.now().plusDays(365)
        );

        mockMvc.perform(
                        post("/api/v1/licenses")
                                .header(
                                        "X-Actor-Id",
                                        actorId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.licenseKey")
                                .value("LIC-TEST123")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                );

        verify(licenseService)
                .createLicense(
                        any(LicenseCreateRequest.class),
                        eq(actorId)
                );
    }


    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid()
            throws Exception {

        mockMvc.perform(
                        post("/api/v1/licenses")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("{}")
                )
                .andExpect(
                        status().isBadRequest()
                );

        verifyNoInteractions(licenseService);
    }


    @Test
    void shouldGetLicenses() throws Exception {

        LicenseResponse response =
                createResponse(
                        UUID.randomUUID(),
                        LicenseStatus.ACTIVE
                );

        when(
                licenseService.getAllLicenses(
                        null,
                        null
                )
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get("/api/v1/licenses")
                                .accept(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].licenseKey")
                                .value("LIC-TEST123")
                )
                .andExpect(
                        jsonPath("$[0].status")
                                .value("ACTIVE")
                );

        verify(licenseService)
                .getAllLicenses(
                        null,
                        null
                );
    }


    @Test
    void shouldGetLicensesWithFilters()
            throws Exception {

        when(
                licenseService.getAllLicenses(
                        "PREMIUM",
                        LicenseStatus.ACTIVE
                )
        ).thenReturn(
                List.of()
        );

        mockMvc.perform(
                        get("/api/v1/licenses")
                                .param(
                                        "plan",
                                        "PREMIUM"
                                )
                                .param(
                                        "status",
                                        "ACTIVE"
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(licenseService)
                .getAllLicenses(
                        "PREMIUM",
                        LicenseStatus.ACTIVE
                );
    }


    @Test
    void shouldGetLicense()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        LicenseResponse response =
                createResponse(
                        licenseId,
                        LicenseStatus.ACTIVE
                );

        when(
                licenseService.getLicense(
                        licenseId
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/licenses/{licenseId}",
                                licenseId
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.licenseKey")
                                .value("LIC-TEST123")
                );

        verify(licenseService)
                .getLicense(licenseId);
    }


    @Test
    void shouldGetLicenseByKey()
            throws Exception {

        String licenseKey =
                "LIC-TEST123";

        LicenseResponse response =
                createResponse(
                        UUID.randomUUID(),
                        LicenseStatus.ACTIVE
                );

        when(
                licenseService.getLicenseByKey(
                        licenseKey
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/licenses/key/{licenseKey}",
                                licenseKey
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.licenseKey")
                                .value(licenseKey)
                );

        verify(licenseService)
                .getLicenseByKey(licenseKey);
    }


    @Test
    void shouldUpdateLicense()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID actorId =
                UUID.randomUUID();

        LicenseResponse response =
                createResponse(
                        licenseId,
                        LicenseStatus.ACTIVE
                );

        when(
                licenseService.updateLicense(
                        eq(licenseId),
                        any(LicenseUpdateRequest.class),
                        eq(actorId)
                )
        ).thenReturn(response);

        String requestBody = """
                {
                    "licensePlan": "STANDARD",
                    "licenseType": "ENTERPRISE",
                    "activationDate": "%s",
                    "expiryDate": "%s"
                }
                """.formatted(
                LocalDate.now(),
                LocalDate.now().plusDays(180)
        );

        mockMvc.perform(
                        put(
                                "/api/v1/licenses/{licenseId}",
                                licenseId
                        )
                                .header(
                                        "X-Actor-Id",
                                        actorId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                );

        verify(licenseService)
                .updateLicense(
                        eq(licenseId),
                        any(LicenseUpdateRequest.class),
                        eq(actorId)
                );
    }


    @Test
    void shouldActivateLicense()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID actorId =
                UUID.randomUUID();

        when(
                licenseService.activateLicense(
                        licenseId,
                        actorId
                )
        ).thenReturn(
                createResponse(
                        licenseId,
                        LicenseStatus.ACTIVE
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/licenses/{licenseId}/activate",
                                licenseId
                        )
                                .header(
                                        "X-Actor-Id",
                                        actorId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(licenseService)
                .activateLicense(
                        licenseId,
                        actorId
                );
    }


    @Test
    void shouldReactivateLicense()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID actorId =
                UUID.randomUUID();

        when(
                licenseService.reactivateLicense(
                        licenseId,
                        actorId
                )
        ).thenReturn(
                createResponse(
                        licenseId,
                        LicenseStatus.ACTIVE
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/licenses/{licenseId}/reactivate",
                                licenseId
                        )
                                .header(
                                        "X-Actor-Id",
                                        actorId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(licenseService)
                .reactivateLicense(
                        licenseId,
                        actorId
                );
    }


    @Test
    void shouldSuspendLicense()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID actorId =
                UUID.randomUUID();

        when(
                licenseService.suspendLicense(
                        licenseId,
                        actorId
                )
        ).thenReturn(
                createResponse(
                        licenseId,
                        LicenseStatus.SUSPENDED
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/licenses/{licenseId}/suspend",
                                licenseId
                        )
                                .header(
                                        "X-Actor-Id",
                                        actorId.toString()
                                )
                )
                .andExpect(
                        status().isOk()
                );

        verify(licenseService)
                .suspendLicense(
                        licenseId,
                        actorId
                );
    }


    @Test
    void shouldRenewLicense()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        UUID actorId =
                UUID.randomUUID();

        LocalDate newExpiry =
                LocalDate.now().plusDays(365);

        when(
                licenseService.renewLicense(
                        eq(licenseId),
                        any(LicenseRenewRequest.class),
                        eq(actorId)
                )
        ).thenReturn(
                createResponse(
                        licenseId,
                        LicenseStatus.ACTIVE
                )
        );

        String requestBody = """
                {
                    "newExpiryDate": "%s"
                }
                """.formatted(newExpiry);

        mockMvc.perform(
                        patch(
                                "/api/v1/licenses/{licenseId}/renew",
                                licenseId
                        )
                                .header(
                                        "X-Actor-Id",
                                        actorId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(
                        status().isOk()
                );

        verify(licenseService)
                .renewLicense(
                        eq(licenseId),
                        any(LicenseRenewRequest.class),
                        eq(actorId)
                );
    }


    @Test
    void shouldGetLicenseStatus()
            throws Exception {

        UUID licenseId =
                UUID.randomUUID();

        LicenseStatusResponse response =
                new LicenseStatusResponse(
                        licenseId,
                        "LIC-TEST123",
                        LicenseStatus.ACTIVE,
                        LocalDate.now(),
                        LocalDate.now().plusDays(365),
                        false
                );

        when(
                licenseService.getLicenseStatus(
                        licenseId
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/licenses/{licenseId}/status",
                                licenseId
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.licenseKey")
                                .value("LIC-TEST123")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                )
                .andExpect(
                        jsonPath("$.expired")
                                .value(false)
                );

        verify(licenseService)
                .getLicenseStatus(licenseId);
    }


    private LicenseResponse createResponse(
            UUID licenseId,
            LicenseStatus status
    ) {

        return new LicenseResponse(
                licenseId,
                "LIC-TEST123",
                "PREMIUM",
                LicenseType.SUBSCRIPTION,
                LocalDate.now(),
                LocalDate.now().plusDays(365),
                status,
                null,
                null
        );
    }
}