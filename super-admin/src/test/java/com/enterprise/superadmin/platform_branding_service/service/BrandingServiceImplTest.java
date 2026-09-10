package com.enterprise.superadmin.platform_branding_service.service;

import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingCreateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.request.BrandingUpdateRequest;
import com.enterprise.superadmin.platform_branding_service.dto.response.BrandingResponse;
import com.enterprise.superadmin.platform_branding_service.entity.Configuration;
import com.enterprise.superadmin.platform_branding_service.exception.BrandingAlreadyExistsException;
import com.enterprise.superadmin.platform_branding_service.exception.BrandingInvalidStateException;
import com.enterprise.superadmin.platform_branding_service.exception.BrandingNotFoundException;
import com.enterprise.superadmin.platform_branding_service.integration.audit.AuditLogClient;
import com.enterprise.superadmin.platform_branding_service.repository.ConfigurationRepository;
import com.enterprise.superadmin.platform_branding_service.security.CurrentUserProvider;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandingServiceImplTest {

    @Mock
    private ConfigurationRepository configurationRepository;

    @Mock
    private BrandingValidationService validationService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AuditLogClient auditLogClient;

    @InjectMocks
    private com.enterprise.superadmin.platform_branding_service.service.impl.BrandingServiceImpl brandingService;

    // ============================================================
    // GET
    // ============================================================

    @Test
    void getCurrentBranding_shouldReturnBranding() {

        Configuration platformName =
                configuration(
                        "branding.platform_name",
                        "Enterprise Platform"
                );

        Configuration companyName =
                configuration(
                        "branding.company_name",
                        "Enterprise Company"
                );

        Configuration status =
                configuration(
                        "branding.status",
                        "DRAFT"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(
                        List.of(
                                platformName,
                                companyName,
                                status
                        )
                );

        BrandingResponse response =
                brandingService.getCurrentBranding();

        assertNotNull(response);

        assertEquals(
                "Enterprise Platform",
                response.getPlatformName()
        );

        assertEquals(
                "Enterprise Company",
                response.getCompanyName()
        );

        assertEquals(
                "DRAFT",
                response.getStatus()
        );
    }

    // ============================================================
    // CREATE
    // ============================================================

    @Test
    void createBranding_shouldCreateSuccessfully() {

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of());

        when(currentUserProvider.getCurrentUserId())
                .thenReturn("admin-user");

        BrandingCreateRequest request =
                validCreateRequest();

        BrandingResponse response =
                brandingService.createBranding(request);

        assertNotNull(response);

        verify(configurationRepository, atLeastOnce())
                .save(any(Configuration.class));

        verify(currentUserProvider)
                .getCurrentUserId();

        verify(auditLogClient)
                .record(
                        eq("admin-user"),
                        eq("BRANDING_CREATED"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM"),
                        eq("SUCCESS"),
                        any()
                );
    }

    @Test
    void createBranding_shouldRejectDuplicateBranding() {

        Configuration existing =
                configuration(
                        "branding.platform_name",
                        "Existing Platform"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of(existing));

        BrandingCreateRequest request =
                validCreateRequest();

        assertThrows(
                BrandingAlreadyExistsException.class,
                () -> brandingService.createBranding(request)
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));

        verifyNoInteractions(auditLogClient);
    }

    @Test
    void createBranding_shouldRejectNullRequest() {

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingService.createBranding(null)
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));
    }

    @Test
    void createBranding_shouldRejectMissingActor() {

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of());

        when(currentUserProvider.getCurrentUserId())
                .thenReturn(" ");

        BrandingCreateRequest request =
                validCreateRequest();

        assertThrows(
                IllegalStateException.class,
                () -> brandingService.createBranding(request)
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));

        verifyNoInteractions(auditLogClient);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Test
    void updateBranding_shouldUpdateSuccessfully() {

        Configuration existing =
                configuration(
                        "branding.platform_name",
                        "Old Platform"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of(existing));

        when(currentUserProvider.getCurrentUserId())
                .thenReturn("admin-user");

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPlatformName("Updated Platform");

        BrandingResponse response =
                brandingService.updateBranding(request);

        assertNotNull(response);

        verify(configurationRepository, atLeastOnce())
                .save(any(Configuration.class));

        verify(auditLogClient)
                .record(
                        eq("admin-user"),
                        eq("BRANDING_UPDATED"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM"),
                        eq("SUCCESS"),
                        any()
                );
    }

    @Test
    void updateBranding_shouldThrowNotFoundWhenBrandingDoesNotExist() {

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of());

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPlatformName("Updated Platform");

        assertThrows(
                BrandingNotFoundException.class,
                () -> brandingService.updateBranding(request)
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));
    }

    @Test
    void updateBranding_shouldRejectNullRequest() {

        assertThrows(
                IllegalArgumentException.class,
                () -> brandingService.updateBranding(null)
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));
    }

    // ============================================================
    // PREVIEW
    // ============================================================

    @Test
    void previewBranding_shouldNotPersistChanges() {

        Configuration platformName =
                configuration(
                        "branding.platform_name",
                        "Current Platform"
                );

        Configuration companyName =
                configuration(
                        "branding.company_name",
                        "Current Company"
                );

        Configuration status =
                configuration(
                        "branding.status",
                        "DRAFT"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(
                        List.of(
                                platformName,
                                companyName,
                                status
                        )
                );

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPlatformName("Preview Platform");

        BrandingResponse response =
                brandingService.previewBranding(request);

        assertNotNull(response);

        assertEquals(
                "Preview Platform",
                response.getPlatformName()
        );

        assertEquals(
                "Current Company",
                response.getCompanyName()
        );

        assertEquals(
                "DRAFT",
                response.getStatus()
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));

        verifyNoInteractions(auditLogClient);
        verifyNoInteractions(currentUserProvider);
    }

    // ============================================================
    // RESET
    // ============================================================

    @Test
    void resetBranding_shouldThrowNotFoundWhenBrandingDoesNotExist() {

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of());

        assertThrows(
                BrandingNotFoundException.class,
                () -> brandingService.resetBranding()
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));
    }

    @Test
    void resetBranding_shouldResetSuccessfully() {

        Configuration existing =
                configuration(
                        "branding.platform_name",
                        "Old Platform"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of(existing));

        when(currentUserProvider.getCurrentUserId())
                .thenReturn("admin-user");

        BrandingResponse response =
                brandingService.resetBranding();

        assertNotNull(response);

        verify(configurationRepository, atLeastOnce())
                .save(any(Configuration.class));

        verify(auditLogClient)
                .record(
                        eq("admin-user"),
                        eq("BRANDING_RESET"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM"),
                        eq("SUCCESS"),
                        any()
                );
    }

    // ============================================================
    // PUBLISH
    // ============================================================

    @Test
    void publishBranding_shouldPublishDraftSuccessfully() {

        Configuration platformName =
                configuration(
                        "branding.platform_name",
                        "Platform"
                );

        Configuration status =
                configuration(
                        "branding.status",
                        "DRAFT"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(
                        List.of(
                                platformName,
                                status
                        )
                );

        when(currentUserProvider.getCurrentUserId())
                .thenReturn("admin-user");

        BrandingResponse response =
                brandingService.publishBranding();

        assertNotNull(response);

        verify(configurationRepository, atLeastOnce())
                .save(any(Configuration.class));

        verify(auditLogClient)
                .record(
                        eq("admin-user"),
                        eq("BRANDING_PUBLISHED"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM"),
                        eq("SUCCESS"),
                        any()
                );
    }

    @Test
    void publishBranding_shouldRejectAlreadyPublishedBranding() {

        Configuration status =
                configuration(
                        "branding.status",
                        "PUBLISHED"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of(status));

        assertThrows(
                BrandingInvalidStateException.class,
                () -> brandingService.publishBranding()
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));

        verifyNoInteractions(auditLogClient);
        verifyNoInteractions(currentUserProvider);
    }

    @Test
    void publishBranding_shouldRejectInvalidState() {

        Configuration status =
                configuration(
                        "branding.status",
                        "ARCHIVED"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of(status));

        assertThrows(
                BrandingInvalidStateException.class,
                () -> brandingService.publishBranding()
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));

        verifyNoInteractions(auditLogClient);
        verifyNoInteractions(currentUserProvider);
    }

    @Test
    void publishBranding_shouldThrowNotFoundWhenNoStatusExists() {

        Configuration platformName =
                configuration(
                        "branding.platform_name",
                        "Platform"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of(platformName));

        assertThrows(
                BrandingNotFoundException.class,
                () -> brandingService.publishBranding()
        );

        verify(configurationRepository, never())
                .save(any(Configuration.class));

        verifyNoInteractions(auditLogClient);
        verifyNoInteractions(currentUserProvider);
    }

    // ============================================================
    // ACTOR
    // ============================================================

    @Test
    void updateBranding_shouldUseAuthenticatedActor() {

        Configuration existing =
                configuration(
                        "branding.platform_name",
                        "Platform"
                );

        when(configurationRepository
                .findByCategoryAndScopeAndIsDeletedFalse(
                        "BRANDING",
                        "PLATFORM"))
                .thenReturn(List.of(existing));

        when(currentUserProvider.getCurrentUserId())
                .thenReturn("super-admin-123");

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPlatformName("Updated Platform");

        brandingService.updateBranding(request);

        verify(currentUserProvider)
                .getCurrentUserId();

        verify(auditLogClient)
                .record(
                        eq("super-admin-123"),
                        eq("BRANDING_UPDATED"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM_BRANDING"),
                        eq("PLATFORM"),
                        eq("SUCCESS"),
                        any()
                );
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private BrandingCreateRequest validCreateRequest() {

        BrandingCreateRequest request =
                new BrandingCreateRequest();

        request.setPlatformName("Enterprise Platform");
        request.setCompanyName("Enterprise Company");
        request.setTagline("Enterprise Cloud Platform");

        request.setLogoUrl(
                "https://example.com/logo.png"
        );

        request.setLoginBackgroundUrl(
                "https://example.com/background.png"
        );

        request.setWelcomeMessage(
                "Welcome to Enterprise Platform"
        );

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

    private Configuration configuration(
            String key,
            String value) {

        Configuration configuration =
                new Configuration();

        configuration.setConfigKey(key);
        configuration.setConfigValue(value);
        configuration.setCategory("BRANDING");
        configuration.setScope("PLATFORM");
        configuration.setStatus("ACTIVE");
        configuration.setIsDeleted(false);

        return configuration;
    }
}