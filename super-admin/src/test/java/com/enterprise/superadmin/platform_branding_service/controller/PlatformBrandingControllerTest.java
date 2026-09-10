package com.enterprise.superadmin.platform_branding_service.controller;

import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingCreateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingUpdateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.response.BrandingResponse;
import com.enterprise.superadmin.platform_branding_service.service.BrandingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlatformBrandingController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlatformBrandingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BrandingService brandingService;

    @Test
    void getCurrentBranding_shouldReturn200() throws Exception {

        when(brandingService.getCurrentBranding())
                .thenReturn(mockBrandingResponse());

        mockMvc.perform(
                        get("/api/v1/branding")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        verify(brandingService)
                .getCurrentBranding();
    }

    @Test
    void createBranding_shouldReturn201() throws Exception {

        BrandingCreateRequest request =
                createValidCreateRequest();

        when(brandingService.createBranding(
                any(BrandingCreateRequest.class)))
                .thenReturn(mockBrandingResponse());

        mockMvc.perform(
                        post("/api/v1/branding")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated());

        verify(brandingService)
                .createBranding(
                        any(BrandingCreateRequest.class)
                );
    }

    @Test
    void updateBranding_shouldReturn200() throws Exception {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPlatformName("Updated Platform");

        when(brandingService.updateBranding(
                any(BrandingUpdateRequest.class)))
                .thenReturn(mockBrandingResponse());

        mockMvc.perform(
                        put("/api/v1/branding")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        verify(brandingService)
                .updateBranding(
                        any(BrandingUpdateRequest.class)
                );
    }

    @Test
    void resetBranding_shouldReturn200() throws Exception {

        when(brandingService.resetBranding())
                .thenReturn(mockBrandingResponse());

        mockMvc.perform(
                        post("/api/v1/branding/reset")
                )
                .andExpect(status().isOk());

        verify(brandingService)
                .resetBranding();
    }

    @Test
    void previewBranding_shouldReturn200() throws Exception {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPlatformName("Preview Platform");
        request.setPrimaryColor("#0052CC");

        when(brandingService.previewBranding(
                any(BrandingUpdateRequest.class)))
                .thenReturn(mockBrandingResponse());

        mockMvc.perform(
                        post("/api/v1/branding/preview")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk());

        verify(brandingService)
                .previewBranding(
                        any(BrandingUpdateRequest.class)
                );
    }

    @Test
    void publishBranding_shouldReturn200() throws Exception {

        when(brandingService.publishBranding())
                .thenReturn(mockBrandingResponse());

        mockMvc.perform(
                        post("/api/v1/branding/publish")
                )
                .andExpect(status().isOk());

        verify(brandingService)
                .publishBranding();
    }

    @Test
    void createBranding_withInvalidRequest_shouldReturn400()
            throws Exception {

        BrandingCreateRequest request =
                new BrandingCreateRequest();

        mockMvc.perform(
                        post("/api/v1/branding")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verify(
                brandingService,
                never()
        ).createBranding(
                any(BrandingCreateRequest.class)
        );
    }

    private BrandingCreateRequest createValidCreateRequest() {

        BrandingCreateRequest request =
                new BrandingCreateRequest();

        request.setPlatformName("Platform");
        request.setCompanyName("Enterprise Company");
        request.setTagline("Enterprise Cloud Platform");

        request.setLogoUrl(
                "https://example.com/logo.png"
        );

        request.setLoginBackgroundUrl(
                "https://example.com/login-background.png"
        );

        request.setWelcomeMessage("Welcome");

        request.setPrimaryColor("#0052CC");
        request.setSecondaryColor("#172B4D");
        request.setAccentColor("#36B37E");

        request.setTheme("LIGHT");

        request.setFaviconUrl(
                "https://example.com/favicon.ico"
        );

        request.setEmailHeaderLogoUrl(
                "https://example.com/email-logo.png"
        );

        request.setFooterText(
                "Enterprise Cloud Platform"
        );

        request.setCopyrightText(
                "© Enterprise"
        );

        return request;
    }

    private BrandingResponse mockBrandingResponse() {

        return new BrandingResponse(
                "Platform",
                "Enterprise Company",
                "Enterprise Cloud Platform",
                "https://example.com/logo.png",
                "https://example.com/login-background.png",
                "Welcome",
                "#0052CC",
                "#172B4D",
                "#36B37E",
                "LIGHT",
                "https://example.com/favicon.ico",
                "https://example.com/email-logo.png",
                "Enterprise Cloud Platform",
                "© Enterprise",
                "DRAFT"
        );
    }
}