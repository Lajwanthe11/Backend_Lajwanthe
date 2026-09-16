package com.example.platformadmin.superadmin.platformconfiguration.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * Request DTO used to create a platform configuration.
 *
 * <p>Contains configuration identity, platform settings, operational configuration,
 * and optional creator/updater audit fields. Enumerated values are represented as
 * {@link String} fields and validated with {@link Pattern} regular expressions.</p>
 *
 * <p>Audit fields ({@code createdBy}, {@code updatedBy}) are optional in the request payload.
 * When omitted, the service layer resolves the caller's username from the authenticated
 * {@code SecurityContextHolder} (defaulting to administrative user {@code "admin"}).</p>
 *
 * <p>Effective date, creation timestamp, and update timestamp are managed authoritatively
 * by the service layer.</p>
 */
public record PlatformConfigurationCreateRequest(

		/**
		 * Name of the platform configuration.
		 */
		@NotBlank(message = "Configuration name is required") @Size(max = 100, message = "Configuration name must not exceed 100 characters") String configurationName,

		/**
		 * Category of the platform configuration.
		 */
		@NotBlank(message = "Configuration category is required") @Size(max = 100, message = "Configuration category must not exceed 100 characters") String configurationCategory,

		/**
		 * Description of the platform configuration.
		 */
		@Size(max = 1000, message = "Description must not exceed 1000 characters") String description,

		/**
		 * Initial status of the platform configuration.
		 */
		@NotBlank(message = "Status is required") @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be either ACTIVE or INACTIVE") String status,

		// ---------------------------------------------------------------------
		// Platform Settings
		// ---------------------------------------------------------------------

		/**
		 * Name of the platform.
		 */
		@NotBlank(message = "Platform name is required") @Size(max = 150, message = "Platform name must not exceed 150 characters") String platformName,

		/**
		 * URL of the platform.
		 */
		@NotBlank(message = "Platform URL is required") @URL(message = "Platform URL must be a valid URL") String platformUrl,

		/**
		 * Environment in which the platform operates.
		 */
		@NotBlank(message = "Environment is required") @Pattern(regexp = "^(DEVELOPMENT|TESTING|STAGING|PRODUCTION)$", message = "Environment must be one of: DEVELOPMENT, TESTING, STAGING, PRODUCTION") String environment,

		/**
		 * Default language used by the platform.
		 */
		@NotBlank(message = "Default language is required") String defaultLanguage,

		/**
		 * Default time zone used by the platform.
		 */
		@NotBlank(message = "Default time zone is required") String defaultTimeZone,

		/**
		 * Default currency used by the platform.
		 */
		@NotBlank(message = "Default currency is required") String defaultCurrency,

		// ---------------------------------------------------------------------
		// Operational Configuration
		// ---------------------------------------------------------------------

		/**
		 * Maintenance mode state.
		 */
		@NotBlank(message = "Maintenance mode is required") @Pattern(regexp = "^(ENABLED|DISABLED)$", message = "Maintenance mode must be either ENABLED or DISABLED") String maintenanceMode,

		/**
		 * Feature toggle state.
		 */
		@NotBlank(message = "Feature toggle is required") @Pattern(regexp = "^(ENABLED|DISABLED)$", message = "Feature toggle must be either ENABLED or DISABLED") String featureToggle,

		/**
		 * Automatic backup state.
		 */
		@NotBlank(message = "Auto backup is required") @Pattern(regexp = "^(ENABLED|DISABLED)$", message = "Auto backup must be either ENABLED or DISABLED") String autoBackup,

		/**
		 * Session timeout.
		 */
		@NotNull(message = "Session timeout is required") @Positive(message = "Session timeout must be a positive integer") Integer sessionTimeout,

		/**
		 * Password expiry.
		 */
		@NotNull(message = "Password expiry is required") @Positive(message = "Password expiry must be a positive integer") Integer passwordExpiry,

		/**
		 * Maximum number of login attempts.
		 */
		@NotNull(message = "Maximum login attempts is required") @Positive(message = "Maximum login attempts must be a positive integer") Integer maximumLoginAttempts
) {
}