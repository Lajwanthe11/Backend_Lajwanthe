package com.example.platformadmin.superadmin.platform_branding_service.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO used to update platform branding.
 *
 * All fields are optional.
 * When a field is supplied, Bean Validation validates its format/size.
 *
 * Business rules such as:
 * - whether the branding exists
 * - whether an update is allowed in the current state
 * - whether the caller is authorized
 *
 * are handled by the service/security layers.
 */

public class BrandingUpdateRequest {

    @Size(max = 100, message = "Platform name must not exceed 100 characters")
    private String platformName;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;

    @Size(max = 255, message = "Tagline must not exceed 255 characters")
    private String tagline;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;

    @Size(max = 500, message = "Login background URL must not exceed 500 characters")
    private String loginBackgroundUrl;

    @Size(max = 250, message = "Welcome message must not exceed 250 characters")
    private String welcomeMessage;

    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Primary color must be a valid HEX color")
    private String primaryColor;

    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Secondary color must be a valid HEX color")
    private String secondaryColor;

    @Pattern(regexp = "^#[A-Fa-f0-9]{6}$", message = "Accent color must be a valid HEX color")
    private String accentColor;

    @Pattern(regexp = "(?i)^(LIGHT|DARK)$", message = "Theme must be either LIGHT or DARK")
    private String theme;

    @Size(max = 500, message = "Favicon URL must not exceed 500 characters")
    private String faviconUrl;

    @Size(max = 500, message = "Email header logo URL must not exceed 500 characters")
    private String emailHeaderLogoUrl;

    @Size(max = 200, message = "Footer text must not exceed 200 characters")
    private String footerText;

    @Size(max = 200, message = "Copyright text must not exceed 200 characters")
    private String copyrightText;

    public BrandingUpdateRequest() {
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getLoginBackgroundUrl() {
        return loginBackgroundUrl;
    }

    public void setLoginBackgroundUrl(String loginBackgroundUrl) {
        this.loginBackgroundUrl = loginBackgroundUrl;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public String getEmailHeaderLogoUrl() {
        return emailHeaderLogoUrl;
    }

    public void setEmailHeaderLogoUrl(String emailHeaderLogoUrl) {
        this.emailHeaderLogoUrl = emailHeaderLogoUrl;
    }

    public String getFooterText() {
        return footerText;
    }

    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    public String getCopyrightText() {
        return copyrightText;
    }

    public void setCopyrightText(String copyrightText) {
        this.copyrightText = copyrightText;
    }
}