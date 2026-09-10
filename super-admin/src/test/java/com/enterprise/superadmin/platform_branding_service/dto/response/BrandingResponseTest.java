package com.enterprise.superadmin.platform_branding_service.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrandingResponseTest {

    @Test
    void constructorAndGetters_shouldReturnExpectedValues() {

        BrandingResponse response = new BrandingResponse(
                "Enterprise Platform",
                "Acme Technologies",
                "Powering Enterprise Operations",
                "https://storage/logo.png",
                "https://storage/background.jpg",
                "Welcome to the platform",
                "#0052CC",
                "#172B4D",
                "#00A3BF",
                "LIGHT",
                "https://storage/favicon.ico",
                "https://storage/email-logo.png",
                "Enterprise Platform",
                "© 2026 Acme Technologies",
                "PUBLISHED"
        );

        assertEquals("Enterprise Platform", response.getPlatformName());
        assertEquals("Acme Technologies", response.getCompanyName());
        assertEquals(
                "Powering Enterprise Operations",
                response.getTagline()
        );
        assertEquals(
                "https://storage/logo.png",
                response.getLogoUrl()
        );
        assertEquals(
                "https://storage/background.jpg",
                response.getLoginBackgroundUrl()
        );
        assertEquals(
                "Welcome to the platform",
                response.getWelcomeMessage()
        );
        assertEquals("#0052CC", response.getPrimaryColor());
        assertEquals("#172B4D", response.getSecondaryColor());
        assertEquals("#00A3BF", response.getAccentColor());
        assertEquals("LIGHT", response.getTheme());
        assertEquals(
                "https://storage/favicon.ico",
                response.getFaviconUrl()
        );
        assertEquals(
                "https://storage/email-logo.png",
                response.getEmailHeaderLogoUrl()
        );
        assertEquals(
                "Enterprise Platform",
                response.getFooterText()
        );
        assertEquals(
                "© 2026 Acme Technologies",
                response.getCopyrightText()
        );
        assertEquals("PUBLISHED", response.getStatus());
    }

    @Test
    void setters_shouldUpdateValues() {

        BrandingResponse response = new BrandingResponse();

        response.setPlatformName("New Platform");
        response.setCompanyName("New Company");
        response.setTheme("DARK");
        response.setStatus("DRAFT");

        assertEquals("New Platform", response.getPlatformName());
        assertEquals("New Company", response.getCompanyName());
        assertEquals("DARK", response.getTheme());
        assertEquals("DRAFT", response.getStatus());
    }

    @Test
    void equals_shouldReturnTrueForSameValues() {

        BrandingResponse first = new BrandingResponse(
                "Platform",
                "Company",
                "Tagline",
                null,
                null,
                null,
                "#FFFFFF",
                "#000000",
                "#123456",
                "LIGHT",
                null,
                null,
                "Footer",
                "Copyright",
                "DRAFT"
        );

        BrandingResponse second = new BrandingResponse(
                "Platform",
                "Company",
                "Tagline",
                null,
                null,
                null,
                "#FFFFFF",
                "#000000",
                "#123456",
                "LIGHT",
                null,
                null,
                "Footer",
                "Copyright",
                "DRAFT"
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void defaultConstructor_shouldCreateEmptyResponse() {

        BrandingResponse response = new BrandingResponse();

        assertNull(response.getPlatformName());
        assertNull(response.getCompanyName());
        assertNull(response.getStatus());
    }
}