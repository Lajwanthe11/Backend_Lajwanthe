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

class BrandingUpdateRequestTest {

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
    void emptyUpdateRequest_shouldBeValid() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void validPartialUpdate_shouldBeValid() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPlatformName("Enterprise Platform");
        request.setPrimaryColor("#0052CC");
        request.setTheme("DARK");

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void platformName_shouldRejectMoreThan100Characters() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPlatformName("a".repeat(101));

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "platformName"
        ));
    }

    @Test
    void companyName_shouldRejectMoreThan100Characters() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setCompanyName("a".repeat(101));

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "companyName"
        ));
    }

    @Test
    void welcomeMessage_shouldRejectMoreThan250Characters() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setWelcomeMessage("a".repeat(251));

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "welcomeMessage"
        ));
    }

    @Test
    void footerText_shouldRejectMoreThan200Characters() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setFooterText("a".repeat(201));

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "footerText"
        ));
    }

    @Test
    void copyrightText_shouldRejectMoreThan200Characters() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setCopyrightText("a".repeat(201));

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "copyrightText"
        ));
    }

    @Test
    void primaryColor_shouldRejectInvalidHex() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setPrimaryColor("#123");

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "primaryColor"
        ));
    }

    @Test
    void secondaryColor_shouldRejectInvalidHex() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setSecondaryColor("red");

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "secondaryColor"
        ));
    }

    @Test
    void accentColor_shouldRejectInvalidHex() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setAccentColor("#FFFF");

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "accentColor"
        ));
    }

    @Test
    void theme_shouldAcceptLight() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setTheme("LIGHT");

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolationFor(
                violations,
                "theme"
        ));
    }

    @Test
    void theme_shouldAcceptDarkCaseInsensitive() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setTheme("dark");

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertFalse(hasViolationFor(
                violations,
                "theme"
        ));
    }

    @Test
    void theme_shouldRejectUnsupportedValue() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setTheme("BLUE");

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "theme"
        ));
    }

    @Test
    void urlFields_shouldRespectMaximumLength() {

        BrandingUpdateRequest request =
                new BrandingUpdateRequest();

        request.setLogoUrl("a".repeat(501));

        Set<ConstraintViolation<BrandingUpdateRequest>> violations =
                validator.validate(request);

        assertTrue(hasViolationFor(
                violations,
                "logoUrl"
        ));
    }

    private static boolean hasViolationFor(
            Set<ConstraintViolation<BrandingUpdateRequest>> violations,
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