package com.enterprise.superadmin.platform_branding_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrandingValidationServiceTest {

    private BrandingValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new BrandingValidationService();
    }

    // =====================================================================
    // PLATFORM NAME
    // =====================================================================

    @Test
    void shouldAcceptValidPlatformName() {

        assertDoesNotThrow(() ->
                validationService.validatePlatformName(
                        "Enterprise Platform"
                )
        );
    }

    @Test
    void shouldRejectNullPlatformName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validatePlatformName(null)
        );
    }

    @Test
    void shouldRejectBlankPlatformName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validatePlatformName("   ")
        );
    }

    @Test
    void shouldRejectPlatformNameExceedingMaximumLength() {

        String platformName = "A".repeat(101);

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validatePlatformName(platformName)
        );
    }

    // =====================================================================
    // COMPANY NAME
    // =====================================================================

    @Test
    void shouldAcceptValidCompanyName() {

        assertDoesNotThrow(() ->
                validationService.validateCompanyName(
                        "ABC Technologies"
                )
        );
    }

    @Test
    void shouldRejectNullCompanyName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCompanyName(null)
        );
    }

    @Test
    void shouldRejectBlankCompanyName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCompanyName("   ")
        );
    }

    @Test
    void shouldRejectCompanyNameExceedingMaximumLength() {

        String companyName = "A".repeat(101);

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCompanyName(companyName)
        );
    }

    // =====================================================================
    // WELCOME MESSAGE
    // =====================================================================

    @Test
    void shouldAcceptValidWelcomeMessage() {

        assertDoesNotThrow(() ->
                validationService.validateWelcomeMessage(
                        "Welcome to Enterprise Platform"
                )
        );
    }

    @Test
    void shouldAcceptNullWelcomeMessage() {

        assertDoesNotThrow(() ->
                validationService.validateWelcomeMessage(null)
        );
    }

    @Test
    void shouldRejectWelcomeMessageExceedingMaximumLength() {

        String welcomeMessage = "A".repeat(251);

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateWelcomeMessage(
                        welcomeMessage
                )
        );
    }

    // =====================================================================
    // FOOTER TEXT
    // =====================================================================

    @Test
    void shouldAcceptValidFooterText() {

        assertDoesNotThrow(() ->
                validationService.validateFooterText(
                        "Enterprise Platform"
                )
        );
    }

    @Test
    void shouldAcceptNullFooterText() {

        assertDoesNotThrow(() ->
                validationService.validateFooterText(null)
        );
    }

    @Test
    void shouldRejectFooterTextExceedingMaximumLength() {

        String footerText = "A".repeat(201);

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateFooterText(
                        footerText
                )
        );
    }

    // =====================================================================
    // COPYRIGHT TEXT
    // =====================================================================

    @Test
    void shouldAcceptValidCopyrightText() {

        assertDoesNotThrow(() ->
                validationService.validateCopyrightText(
                        "© 2026 ABC Technologies"
                )
        );
    }

    @Test
    void shouldAcceptNullCopyrightText() {

        assertDoesNotThrow(() ->
                validationService.validateCopyrightText(null)
        );
    }

    @Test
    void shouldRejectCopyrightTextExceedingMaximumLength() {

        String copyrightText = "A".repeat(201);

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateCopyrightText(
                        copyrightText
                )
        );
    }

    // =====================================================================
    // HEX COLORS
    // =====================================================================

    @Test
    void shouldAcceptValidHexColor() {

        assertDoesNotThrow(() ->
                validationService.validateHexColor(
                        "primary_color",
                        "#1976D2"
                )
        );
    }

    @Test
    void shouldAcceptLowercaseHexColor() {

        assertDoesNotThrow(() ->
                validationService.validateHexColor(
                        "primary_color",
                        "#abcdef"
                )
        );
    }

    @Test
    void shouldRejectHexColorWithoutHash() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateHexColor(
                        "primary_color",
                        "1976D2"
                )
        );
    }

    @Test
    void shouldRejectInvalidHexColorCharacters() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateHexColor(
                        "primary_color",
                        "#GGGGGG"
                )
        );
    }

    @Test
    void shouldRejectShortHexColor() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateHexColor(
                        "primary_color",
                        "#FFF"
                )
        );
    }

    @Test
    void shouldRejectLongHexColor() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateHexColor(
                        "primary_color",
                        "#1234567"
                )
        );
    }

    @Test
    void shouldRejectNullHexColor() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateHexColor(
                        "primary_color",
                        null
                )
        );
    }

    // =====================================================================
    // THEME
    // =====================================================================

    @Test
    void shouldAcceptLightTheme() {

        assertDoesNotThrow(() ->
                validationService.validateTheme("LIGHT")
        );
    }

    @Test
    void shouldAcceptDarkTheme() {

        assertDoesNotThrow(() ->
                validationService.validateTheme("DARK")
        );
    }

    @Test
    void shouldAcceptLowercaseTheme() {

        assertDoesNotThrow(() ->
                validationService.validateTheme("light")
        );

        assertDoesNotThrow(() ->
                validationService.validateTheme("dark")
        );
    }

    @Test
    void shouldRejectInvalidTheme() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateTheme("BLUE")
        );
    }

    @Test
    void shouldRejectNullTheme() {

        assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validateTheme(null)
        );
    }
}