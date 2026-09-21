package com.example.platformadmin.superadmin.platform_branding_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Centralized business validation for Platform Branding.
 *
 * Responsibilities:
 * - Validate branding business rules that are shared across create/update flows.
 * - Keep validation logic out of controller/service orchestration.
 * - Provide consistent validation messages.
 *
 * Note:
 * File extension/MIME/size validation belongs to the asset/storage layer,
 * not this class.
 */
@Service
public class BrandingValidationService {

    private static final Logger log = LoggerFactory.getLogger(BrandingValidationService.class);

    private static final int MAX_PLATFORM_NAME_LENGTH = 100;
    private static final int MAX_COMPANY_NAME_LENGTH = 100;
    private static final int MAX_WELCOME_MESSAGE_LENGTH = 250;
    private static final int MAX_FOOTER_TEXT_LENGTH = 200;
    private static final int MAX_COPYRIGHT_TEXT_LENGTH = 200;

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[A-Fa-f0-9]{6}$");

    /**
     * Allowed theme values based on the Platform Branding requirement.
     */
    private static final String LIGHT_THEME = "LIGHT";
    private static final String DARK_THEME = "DARK";

    public void validatePlatformName(String platformName) {
        validateRequiredText(platformName, "platform_name", MAX_PLATFORM_NAME_LENGTH);
    }

    public void validateCompanyName(String companyName) {
        validateRequiredText(companyName, "company_name", MAX_COMPANY_NAME_LENGTH);
    }

    public void validateWelcomeMessage(String welcomeMessage) {
        validateOptionalTextLength(welcomeMessage, "welcome_message", MAX_WELCOME_MESSAGE_LENGTH);
    }

    public void validateFooterText(String footerText) {
        validateOptionalTextLength(footerText, "footer_text", MAX_FOOTER_TEXT_LENGTH);
    }

    public void validateCopyrightText(String copyrightText) {
        validateOptionalTextLength(copyrightText, "copyright_text", MAX_COPYRIGHT_TEXT_LENGTH);
    }

    public void validateHexColor(String fieldName, String color) {

        if (!StringUtils.hasText(color)) {
            throw validationException(fieldName, "Color value must not be blank");
        }

        String normalizedColor = color.trim();

        if (!HEX_COLOR_PATTERN.matcher(normalizedColor).matches()) {
            log.warn("Branding validation failed for field='{}': invalid HEX format", fieldName);

            throw validationException(fieldName, "Color must be a valid 6-digit HEX value such as #0052CC");
        }
        log.debug("Branding HEX color validation successful for field='{}'", fieldName);
    }

    public void validateOptionalHexColor(String fieldName, String color) {
        if (!StringUtils.hasText(color)) {
            return;
        }
        validateHexColor(fieldName, color);
    }

    public void validateTheme(String theme) {

        if (!StringUtils.hasText(theme)) {
            throw validationException("theme", "Theme must be provided");
        }

        String normalizedTheme = theme.trim().toUpperCase(Locale.ROOT);

        if (!LIGHT_THEME.equals(normalizedTheme)
                && !DARK_THEME.equals(normalizedTheme)) {

            log.warn("Branding validation failed for field='theme': unsupported value='{}'", normalizedTheme);

            throw validationException("theme", "Theme must be LIGHT or DARK");
        }

        log.debug("Branding theme validation successful: theme='{}'", normalizedTheme);
    }

    public void validateOptionalTheme(String theme) {
        if (!StringUtils.hasText(theme)) {
            return;
        }
        validateTheme(theme);
    }

    private void validateRequiredText(String value, String fieldName, int maxLength) {

        if (!StringUtils.hasText(value)) {
            log.warn("Branding validation failed for field='{}': required value is blank", fieldName);
            throw validationException(fieldName, fieldName + " is required");
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > maxLength) {
            log.warn("Branding validation failed for field='{}': length={} exceeds maxLength={}", fieldName, normalizedValue.length(), maxLength);
            throw validationException(fieldName, fieldName + " must not exceed " + maxLength + " characters");
        }

        log.debug("Branding validation successful for required field='{}'", fieldName);
    }

    private void validateOptionalTextLength(String value, String fieldName, int maxLength) {

        if (!StringUtils.hasText(value)) {
            return;
        }
        String normalizedValue = value.trim();

        if (normalizedValue.length() > maxLength) {
            log.warn("Branding validation failed for field='{}': length={} exceeds maxLength={}", fieldName, normalizedValue.length(), maxLength);
            throw validationException(fieldName, fieldName + " must not exceed " + maxLength + " characters");
        }

        log.debug("Branding validation successful for optional field='{}'", fieldName);
    }

    private IllegalArgumentException validationException(String fieldName, String message) {
        return new IllegalArgumentException("Invalid branding field '" + fieldName + "': " + message);
    }
}