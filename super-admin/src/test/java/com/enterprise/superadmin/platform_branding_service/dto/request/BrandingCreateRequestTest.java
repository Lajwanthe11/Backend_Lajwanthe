package com.enterprise.superadmin.platform_branding_service.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BrandingCreateRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void validRequest_shouldHaveNoViolations() {

        BrandingCreateRequest request = validRequest();

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(
                violations.isEmpty(),
                "Valid request should not contain validation errors"
        );
    }

    @Test
    void platformName_shouldBeRequired() {

        BrandingCreateRequest request = validRequest();
        request.setPlatformName(null);

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "platformName"
        ));
    }

    @Test
    void companyName_shouldBeRequired() {

        BrandingCreateRequest request = validRequest();
        request.setCompanyName("");

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "companyName"
        ));
    }

    @Test
    void platformName_shouldNotExceed100Characters() {

        BrandingCreateRequest request = validRequest();
        request.setPlatformName("a".repeat(101));

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "platformName"
        ));
    }

    @Test
    void companyName_shouldNotExceed100Characters() {

        BrandingCreateRequest request = validRequest();
        request.setCompanyName("a".repeat(101));

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "companyName"
        ));
    }

    @Test
    void welcomeMessage_shouldNotExceed250Characters() {

        BrandingCreateRequest request = validRequest();
        request.setWelcomeMessage("a".repeat(251));

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "welcomeMessage"
        ));
    }

    @Test
    void footerText_shouldNotExceed200Characters() {

        BrandingCreateRequest request = validRequest();
        request.setFooterText("a".repeat(201));

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "footerText"
        ));
    }

    @Test
    void copyrightText_shouldNotExceed200Characters() {

        BrandingCreateRequest request = validRequest();
        request.setCopyrightText("a".repeat(201));

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "copyrightText"
        ));
    }

    @Test
    void primaryColor_shouldAcceptValidHex() {

        BrandingCreateRequest request = validRequest();
        request.setPrimaryColor("#0052CC");

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolationFor(
                violations,
                "primaryColor"
        ));
    }

    @Test
    void primaryColor_shouldRejectInvalidHex() {

        BrandingCreateRequest request = validRequest();
        request.setPrimaryColor("#123");

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "primaryColor"
        ));
    }

    @Test
    void accentColor_shouldRejectInvalidHex() {

        BrandingCreateRequest request = validRequest();
        request.setAccentColor("blue");

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "accentColor"
        ));
    }

    @Test
    void theme_shouldAcceptLight() {

        BrandingCreateRequest request = validRequest();
        request.setTheme("LIGHT");

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolationFor(
                violations,
                "theme"
        ));
    }

    @Test
    void theme_shouldAcceptDarkCaseInsensitive() {

        BrandingCreateRequest request = validRequest();
        request.setTheme("dark");

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolationFor(
                violations,
                "theme"
        ));
    }

    @Test
    void theme_shouldRejectUnsupportedValue() {

        BrandingCreateRequest request = validRequest();
        request.setTheme("BLUE");

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "theme"
        ));
    }

    @Test
    void secondaryColor_shouldRejectInvalidHex() {

        BrandingCreateRequest request = validRequest();
        request.setSecondaryColor("#GGGGGG");

        Set<ConstraintViolation<BrandingCreateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "secondaryColor"
        ));
    }

    private static BrandingCreateRequest validRequest() {

        BrandingCreateRequest request =
                new BrandingCreateRequest();

        request.setPlatformName("Enterprise Platform");
        request.setCompanyName("Acme Technologies");
        request.setTagline("Enterprise management platform");

        request.setLogoUrl(
                "https://storage.example.com/logo.png"
        );

        request.setLoginBackgroundUrl(
                "https://storage.example.com/background.jpg"
        );

        request.setWelcomeMessage(
                "Welcome to the Enterprise Platform"
        );

        request.setPrimaryColor("#0052CC");
        request.setSecondaryColor("#172B4D");
        request.setAccentColor("#00A3BF");

        request.setTheme("LIGHT");

        request.setFaviconUrl(
                "https://storage.example.com/favicon.ico"
        );

        request.setEmailHeaderLogoUrl(
                "https://storage.example.com/email-logo.png"
        );

        request.setFooterText("Enterprise Platform");

        request.setCopyrightText(
                "© 2026 Acme Technologies"
        );

        return request;
    }

    private static boolean hasViolationFor(
            Set<ConstraintViolation<BrandingCreateRequest>> violations,
            String property
    ) {
        return violations.stream()
                .anyMatch(
                        violation ->
                                violation.getPropertyPath()
                                        .toString()
                                        .equals(property)
                );
    }
}