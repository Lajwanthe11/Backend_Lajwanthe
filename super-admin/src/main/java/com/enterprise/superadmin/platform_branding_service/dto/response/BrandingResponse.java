package com.enterprise.superadmin.platform_branding_service.dto.response;

import java.util.Objects;

/**
 * API response representing the current platform branding configuration.
 *
 * This DTO is intentionally independent from the persistence model.
 * The API must not expose Configuration entities directly.
 */
public class BrandingResponse {

    private String platformName;
    private String companyName;
    private String tagline;

    private String logoUrl;
    private String loginBackgroundUrl;

    private String welcomeMessage;

    private String primaryColor;
    private String secondaryColor;
    private String accentColor;

    private String theme;

    private String faviconUrl;
    private String emailHeaderLogoUrl;

    private String footerText;
    private String copyrightText;

    private String status;

    public BrandingResponse() {
    }

    public BrandingResponse(
            String platformName,
            String companyName,
            String tagline,
            String logoUrl,
            String loginBackgroundUrl,
            String welcomeMessage,
            String primaryColor,
            String secondaryColor,
            String accentColor,
            String theme,
            String faviconUrl,
            String emailHeaderLogoUrl,
            String footerText,
            String copyrightText,
            String status
    ) {
        this.platformName = platformName;
        this.companyName = companyName;
        this.tagline = tagline;
        this.logoUrl = logoUrl;
        this.loginBackgroundUrl = loginBackgroundUrl;
        this.welcomeMessage = welcomeMessage;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.accentColor = accentColor;
        this.theme = theme;
        this.faviconUrl = faviconUrl;
        this.emailHeaderLogoUrl = emailHeaderLogoUrl;
        this.footerText = footerText;
        this.copyrightText = copyrightText;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BrandingResponse that)) {
            return false;
        }

        return Objects.equals(platformName, that.platformName)
                && Objects.equals(companyName, that.companyName)
                && Objects.equals(tagline, that.tagline)
                && Objects.equals(logoUrl, that.logoUrl)
                && Objects.equals(loginBackgroundUrl, that.loginBackgroundUrl)
                && Objects.equals(welcomeMessage, that.welcomeMessage)
                && Objects.equals(primaryColor, that.primaryColor)
                && Objects.equals(secondaryColor, that.secondaryColor)
                && Objects.equals(accentColor, that.accentColor)
                && Objects.equals(theme, that.theme)
                && Objects.equals(faviconUrl, that.faviconUrl)
                && Objects.equals(emailHeaderLogoUrl, that.emailHeaderLogoUrl)
                && Objects.equals(footerText, that.footerText)
                && Objects.equals(copyrightText, that.copyrightText)
                && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                platformName,
                companyName,
                tagline,
                logoUrl,
                loginBackgroundUrl,
                welcomeMessage,
                primaryColor,
                secondaryColor,
                accentColor,
                theme,
                faviconUrl,
                emailHeaderLogoUrl,
                footerText,
                copyrightText,
                status
        );
    }

    @Override
    public String toString() {
        return "BrandingResponse{" +
                "platformName='" + platformName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", tagline='" + tagline + '\'' +
                ", logoUrl='" + logoUrl + '\'' +
                ", loginBackgroundUrl='" + loginBackgroundUrl + '\'' +
                ", welcomeMessage='" + welcomeMessage + '\'' +
                ", primaryColor='" + primaryColor + '\'' +
                ", secondaryColor='" + secondaryColor + '\'' +
                ", accentColor='" + accentColor + '\'' +
                ", theme='" + theme + '\'' +
                ", faviconUrl='" + faviconUrl + '\'' +
                ", emailHeaderLogoUrl='" + emailHeaderLogoUrl + '\'' +
                ", footerText='" + footerText + '\'' +
                ", copyrightText='" + copyrightText + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}