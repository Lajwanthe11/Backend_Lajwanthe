package com.example.platformadmin.superadmin.platformconfiguration.service;

import com.example.common.security.user.JwtUserPrincipal;
import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationCreateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationStatusUpdateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.request.PlatformConfigurationUpdateRequest;
import com.example.platformadmin.superadmin.platformconfiguration.dto.response.PlatformConfigurationHistoryResponse;
import com.example.platformadmin.superadmin.platformconfiguration.dto.response.PlatformConfigurationResponse;
import com.example.platformadmin.superadmin.platformconfiguration.entity.PlatformConfiguration;
import com.example.platformadmin.superadmin.platformconfiguration.entity.PlatformConfigurationHistory;
import com.example.platformadmin.superadmin.platformconfiguration.exception.ConfigurationActivationException;
import com.example.platformadmin.superadmin.platformconfiguration.exception.DuplicateConfigurationNameException;
import com.example.platformadmin.superadmin.platformconfiguration.exception.InvalidConfigurationValueException;
import com.example.platformadmin.superadmin.platformconfiguration.exception.PlatformConfigurationNotFoundException;
import com.example.platformadmin.superadmin.platformconfiguration.repository.PlatformConfigurationHistoryRepository;
import com.example.platformadmin.superadmin.platformconfiguration.repository.PlatformConfigurationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enterprise Service implementation for Platform Configuration management operations.
 *
 * <p><b>FRS Traceability:</b></p>
 * <ul>
 * <li><b>Module:</b> Module 1 &mdash; Platform Administration</li>
 * <li><b>Feature:</b> FR-001.2 Platform Configuration</li>
 * <li><b>Acceptance Criteria:</b> AC-0009 (Create/Update), AC-0010 (Validation),
 *     AC-0011 (Audit/History), AC-0015 (Defaults), AC-0016 (Status &amp; Soft Delete)</li>
 * <li><b>Business Rules:</b> BR-0011 (Soft Delete), BR-0012 (Unique Active Name),
 *     BR-0016 (Revision History &amp; Rollback), BR-0017 (Validation Gate), BR-0020 (Pre-Activation Validation)</li>
 * <li><b>Validation Standards:</b> VAL-0007 (Uniqueness), VAL-0008 (Formatting),
 *     VAL-0010 (Operational Bounds), VAL-0012 (Pre-Activation Safeguards)</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class PlatformConfigurationServiceImpl implements PlatformConfigurationService {

	private static final Logger log = LoggerFactory.getLogger(PlatformConfigurationServiceImpl.class);

	// Default configuration values for restore operation
	public static final String DEFAULT_PLATFORM_NAME = "One Enterprise Cloud Platform";
	public static final String DEFAULT_PLATFORM_URL = "https://platform.enterprise.com";
	public static final String DEFAULT_ENVIRONMENT = "DEVELOPMENT";
	public static final String DEFAULT_LANGUAGE = "en";
	public static final String DEFAULT_TIME_ZONE = "UTC";
	public static final String DEFAULT_CURRENCY = "USD";
	public static final String DEFAULT_MAINTENANCE_MODE = "DISABLED";
	public static final String DEFAULT_FEATURE_TOGGLE = "ENABLED";
	public static final String DEFAULT_AUTO_BACKUP = "ENABLED";
	public static final int DEFAULT_SESSION_TIMEOUT = 30;
	public static final int DEFAULT_PASSWORD_EXPIRY = 90;
	public static final int DEFAULT_MAX_LOGIN_ATTEMPTS = 5;

	// Allowed value sets for runtime business validation
	private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "INACTIVE");
	private static final Set<String> ALLOWED_ENVIRONMENTS = Set.of("DEVELOPMENT", "TESTING", "STAGING", "PRODUCTION");
	private static final Set<String> ALLOWED_TOGGLES = Set.of("ENABLED", "DISABLED");
	private static final Set<String> ALLOWED_CATEGORIES = Set.of(
			"SYSTEM", "SECURITY", "INTEGRATION", "COMMUNICATION", "GENERAL", "PLATFORM", "DATABASE"
	);

	// ISO & IANA master data validation sets
	private static final Set<String> ISO_LANGUAGES = new HashSet<>(Arrays.asList(Locale.getISOLanguages()));
	private static final Set<String> ISO_CURRENCIES = Currency.getAvailableCurrencies().stream()
			.map(Currency::getCurrencyCode)
			.collect(Collectors.toSet());
	private static final Set<String> IANA_TIME_ZONES = ZoneId.getAvailableZoneIds();
	 // minutes

	private final PlatformConfigurationRepository repository;
	private final PlatformConfigurationHistoryRepository historyRepository;
	
	
	//@Autowired(required = false)
	//private AuditServiceClient auditServiceClient;

	public PlatformConfigurationServiceImpl(PlatformConfigurationRepository repository) {
		this(repository, null);
	}

	@Autowired
	public PlatformConfigurationServiceImpl(
	        PlatformConfigurationRepository repository,
	        PlatformConfigurationHistoryRepository historyRepository) {
	    this.repository = repository;
	    this.historyRepository = historyRepository;
	}
		

	private UUID getCurrentUserId() {
	    // Return authenticated user UUID, or fallback system UUID:
	    return UUID.fromString("00000000-0000-0000-0000-000000000000");
	}

	/**
	 * Searches and filters non-deleted platform configurations based on dynamic
	 * criteria.
	 *
	 * @param name        optional configuration name query string (partial
	 *                    case-insensitive matching)
	 * @param category    optional category (case-insensitive exact match)
	 * @param environment optional operational environment tier
	 * @param status      optional lifecycle status (ACTIVE or INACTIVE)
	 * @return list of matching {@link PlatformConfigurationResponse} DTOs
	 */
	@Override
	public List<PlatformConfigurationResponse> getAll(String name, String category, String environment, String status) {
		log.debug("Searching configurations: name='{}', category='{}', env={}, status={}", name, category, environment,
				status);
		Specification<PlatformConfiguration> spec = PlatformConfigurationSpecification.filterBy(name, category,
				environment, status);
		List<PlatformConfigurationResponse> results = repository.findAll(spec).stream().map(this::toResponse).toList();
		log.debug("Found {} platform configuration(s) matching criteria", results.size());
		return results;
	}

	/**
	 * Retrieves an active (non-soft-deleted) platform configuration by its unique
	 * identifier.
	 *
	 * @param id unique identifier of the target configuration
	 * @return {@link PlatformConfigurationResponse} representation of the
	 *         configuration
	 * @throws PlatformConfigurationNotFoundException if the configuration does not
	 *                                                exist or is soft-deleted
	 */
	@Override
	public PlatformConfigurationResponse getById(UUID id) {
		log.debug("Fetching platform configuration by id: {}", id);
		PlatformConfiguration config = findActiveConfiguration(id);
		log.debug("Successfully resolved platform configuration: id={}, name='{}'", id, config.getConfigurationName());
		return toResponse(config);
	}

	/**
	 * Creates and persists a new platform configuration after validating business
	 * constraints.
	 *
	 * <p>
	 * Enforces BR-0012: Configuration Name must be unique among active
	 * configurations.
	 * </p>
	 *
	 * @param request valid payload containing initial configuration values
	 * @return created {@link PlatformConfigurationResponse} DTO
	 * @throws DuplicateConfigurationNameException if another active configuration
	 *                                             already uses the specified name
	 */
	@Override
	@Transactional
	public PlatformConfigurationResponse create(PlatformConfigurationCreateRequest request) {
		log.info("Creating platform configuration: name='{}', category='{}'", request.configurationName(),
				request.configurationCategory());

		// BR-0012: Ensure configuration name uniqueness across active records
		validateUniqueConfigurationNameForCreate(request.configurationName());

		// Validate allowed string values
		validateConfigurationValues(request.status(), request.environment(), request.maintenanceMode(),
				request.featureToggle(), request.autoBackup());

		// Validate master data fields (category, language, timezone, currency)
		validateMasterData(request.configurationCategory(), request.defaultLanguage(),
				request.defaultTimeZone(), request.defaultCurrency());

		// Pre-activation validation if creating in ACTIVE status (VAL-0012, BR-0020)
		if ("ACTIVE".equalsIgnoreCase(request.status())) {
			validatePreActivation(request.environment(), request.autoBackup(),
					request.sessionTimeout(), request.passwordExpiry());
		}
		


		LocalDate effectiveDate = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();
		PlatformConfiguration config = new PlatformConfiguration();
		config.setConfigurationName(request.configurationName());
		config.setConfigurationCategory(request.configurationCategory());
		config.setDescription(request.description());
		config.setStatus(request.status().trim().toUpperCase());
		config.setEffectiveDate(effectiveDate);

		// Platform Settings
		config.setPlatformName(request.platformName());
		config.setPlatformUrl(request.platformUrl());
		config.setEnvironment(request.environment().trim().toUpperCase());
		config.setDefaultLanguage(request.defaultLanguage());
		config.setDefaultTimeZone(request.defaultTimeZone());
		config.setDefaultCurrency(request.defaultCurrency());

		// Operational Configuration
		config.setMaintenanceMode(request.maintenanceMode().trim().toUpperCase());
		config.setFeatureToggle(request.featureToggle().trim().toUpperCase());
		config.setAutoBackup(request.autoBackup().trim().toUpperCase());
		config.setSessionTimeout(request.sessionTimeout());
		config.setPasswordExpiry(request.passwordExpiry());
		config.setMaximumLoginAttempts(request.maximumLoginAttempts());

		// Audit Metadata automatically populated from SecurityContext
		String currentUser = getCurrentUsername();

		config.setCreatedAt(now);
		config.setCreatedBy(currentUser);
		config.setUpdatedAt(now);
		config.setUpdatedBy(currentUser);
		config.setDeleted(false);
		config.setVersion(1);

		PlatformConfiguration saved = repository.save(config);
		if (saved == null) {
			saved = config;
		}
		recordSnapshot(saved, "INITIAL_CREATION", currentUser);
		log.info("Platform configuration successfully created with id: {}, version: {}, createdBy: '{}'", saved.getId(),
				saved.getVersion(), currentUser);
		//logAudit(currentUser,"CONFIGURATION_CREATED", "Completed");
		return toResponse(saved);
	}

	/**
	 * Updates an existing non-deleted platform configuration.
	 *
	 * <p>
	 * Enforces BR-0012: Configuration Name must remain unique across other active
	 * configurations.
	 * </p>
	 *
	 * @param id      unique identifier of the configuration to update
	 * @param request valid payload containing modified configuration values
	 * @return updated {@link PlatformConfigurationResponse} DTO
	 * @throws PlatformConfigurationNotFoundException if the configuration does not
	 *                                                exist or is soft-deleted
	 * @throws DuplicateConfigurationNameException    if the updated name conflicts
	 *                                                with another active
	 *                                                configuration
	 */
	@Override
	@Transactional
	public PlatformConfigurationResponse update(UUID id, PlatformConfigurationUpdateRequest request) {
		log.info("Updating platform configuration: id={}, newName='{}'", id, request.configurationName());

		PlatformConfiguration config = findActiveConfiguration(id);

		// BR-0012: Ensure updated configuration name does not collide with another
		// active record
		validateUniqueConfigurationNameForUpdate(request.configurationName(), id);

		// Validate allowed string values
		validateConfigurationValues(request.status(), request.environment(), request.maintenanceMode(),
				request.featureToggle(), request.autoBackup());

		// Validate master data fields (category, language, timezone, currency)
		validateMasterData(request.configurationCategory(), request.defaultLanguage(),
				request.defaultTimeZone(), request.defaultCurrency());

		// Pre-activation validation if updating to ACTIVE status (VAL-0012, BR-0020)
		if ("ACTIVE".equalsIgnoreCase(request.status())) {
			validatePreActivation(request.environment(), request.autoBackup(),
					request.sessionTimeout(), request.passwordExpiry());
		}


		config.setConfigurationName(request.configurationName());
		config.setConfigurationCategory(request.configurationCategory());
		config.setDescription(request.description());
		config.setStatus(request.status().trim().toUpperCase());

		// Update Platform Settings
		config.setPlatformName(request.platformName());
		config.setPlatformUrl(request.platformUrl());
		config.setEnvironment(request.environment().trim().toUpperCase());
		config.setDefaultLanguage(request.defaultLanguage());
		config.setDefaultTimeZone(request.defaultTimeZone());
		config.setDefaultCurrency(request.defaultCurrency());

		// Update Operational Configuration
		config.setMaintenanceMode(request.maintenanceMode().trim().toUpperCase());
		config.setFeatureToggle(request.featureToggle().trim().toUpperCase());
		config.setAutoBackup(request.autoBackup().trim().toUpperCase());
		config.setSessionTimeout(request.sessionTimeout());
		config.setPasswordExpiry(request.passwordExpiry());
		config.setMaximumLoginAttempts(request.maximumLoginAttempts());

		// Update Audit Metadata from SecurityContext and Increment Version (BR-0016,
		// AC-0011)
		String currentUser = getCurrentUsername();
		config.setUpdatedAt(LocalDateTime.now());
		config.setUpdatedBy(currentUser);
		config.setVersion(config.getVersion() != null ? config.getVersion() + 1 : 2);

		PlatformConfiguration updated = repository.save(config);
		if (updated == null) {
			updated = config;
		}
		recordSnapshot(updated, "CONFIGURATION_UPDATE", currentUser);
		log.info("Platform configuration successfully updated: id={}, version={}, updatedBy='{}'", updated.getId(),
				updated.getVersion(), currentUser);
		//logAudit(currentUser,"CONFIGURATION_UPDATED", "Completed");

		return toResponse(updated);
	}

	/**
	 * Updates the lifecycle status (ACTIVE or INACTIVE) of an existing platform
	 * configuration.
	 *
	 * @param id      unique identifier of the configuration whose status to update
	 * @param request valid payload containing target status and updater username
	 * @return updated {@link PlatformConfigurationResponse} DTO
	 * @throws PlatformConfigurationNotFoundException if the configuration does not
	 *                                                exist or is soft-deleted
	 */
	@Override
	@Transactional
	public PlatformConfigurationResponse updateStatus(UUID id, PlatformConfigurationStatusUpdateRequest request) {
		log.info("Updating status for configuration id: {} to {}", id, request.status());

		validateStatus(request.status());

		PlatformConfiguration config = findActiveConfiguration(id);
		String currentUser = getCurrentUsername();
		String normalizedStatus = request.status().trim().toUpperCase();

		// Idempotent status update: if already in the target status, return early without version bump
		if (normalizedStatus.equalsIgnoreCase(config.getStatus())) {
			log.info("Configuration id: {} is already in status: {}. Idempotent status update, returning existing state.", id, normalizedStatus);
			return toResponse(config);
		}

		// Pre-activation validation when transitioning to ACTIVE (VAL-0012, BR-0020)
		if ("ACTIVE".equalsIgnoreCase(normalizedStatus)) {
			validatePreActivation(config.getEnvironment(), config.getAutoBackup(),
					config.getSessionTimeout(), config.getPasswordExpiry());
		}

		config.setStatus(normalizedStatus);
		config.setUpdatedAt(LocalDateTime.now());
		config.setUpdatedBy(currentUser);
		config.setVersion(config.getVersion() != null ? config.getVersion() + 1 : 2);

		PlatformConfiguration updated = repository.save(config);
		if (updated == null) {
			updated = config;
		}
		recordSnapshot(updated, "STATUS_CHANGE_" + normalizedStatus, currentUser);
		log.info("Platform configuration status updated: id={}, status={}, version={}, updatedBy='{}'", updated.getId(),
				updated.getStatus(), updated.getVersion(), currentUser);
		//logAudit(currentUser,"CONFIGURATION_STATUS_UPDATED", "Completed");

		
		return toResponse(updated);
	}

	/**
	 * Soft-deletes an existing platform configuration by marking
	 * {@code deleted = true}.
	 *
	 * <p>
	 * Enforces soft deletion (FR-001.2, BR-11, AC-0016). Physical database records
	 * are preserved.
	 * </p>
	 *
	 * @param id unique identifier of the platform configuration to soft delete
	 * @throws PlatformConfigurationNotFoundException if the configuration does not
	 *                                                exist or is already
	 *                                                soft-deleted
	 */
	@Override
	@Transactional
	public void delete(UUID id) {
		log.info("Soft deleting platform configuration with id: {}", id);

		PlatformConfiguration config = findActiveConfiguration(id);
		String deleter = getCurrentUsername();
		config.setDeleted(true);
		config.setDeletedAt(LocalDateTime.now());
		config.setDeletedBy(deleter);
		config.setStatus("INACTIVE");
		config.setVersion(config.getVersion() != null ? config.getVersion() + 1 : 2);

		PlatformConfiguration saved = repository.save(config);
		if (saved == null) {
			saved = config;
		}
		recordSnapshot(saved, "CONFIGURATION_DELETED", deleter);
		//logAudit(deleter,"CONFIGURATION_DELETED", "Completed");

		log.info("Platform configuration soft deleted: id={}, version={}, deletedBy={}", id, saved.getVersion(), deleter);
	}

	/**
	 * Restores an existing platform configuration to standard enterprise default
	 * settings.
	 *
	 * <p>
	 * Preserves configuration identity (UUID), unique name, category, and lifecycle
	 * status (AC-0015, BR-0012).
	 * </p>
	 *
	 * @param id unique identifier of the platform configuration to restore
	 * @return restored {@link PlatformConfigurationResponse} DTO
	 * @throws PlatformConfigurationNotFoundException if the configuration does not
	 *                                                exist or is soft-deleted
	 */
	@Override
	@Transactional
	public PlatformConfigurationResponse restoreDefaultConfiguration(UUID id) {
		log.info("Restoring platform configuration to defaults: id={}", id);

		PlatformConfiguration config = findActiveConfiguration(id);

		// Preserve unique name, identity, and status to satisfy database uniqueness
		// (BR-0012)
		config.setDescription("Restored to platform default settings");

		// Restore Platform Settings to standard enterprise defaults
		config.setPlatformName(DEFAULT_PLATFORM_NAME);
		config.setPlatformUrl(DEFAULT_PLATFORM_URL);
		config.setEnvironment(DEFAULT_ENVIRONMENT);
		config.setDefaultLanguage(DEFAULT_LANGUAGE);
		config.setDefaultTimeZone(DEFAULT_TIME_ZONE);
		config.setDefaultCurrency(DEFAULT_CURRENCY);

		// Restore Operational Settings to standard enterprise defaults
		config.setMaintenanceMode(DEFAULT_MAINTENANCE_MODE);
		config.setFeatureToggle(DEFAULT_FEATURE_TOGGLE);
		config.setAutoBackup(DEFAULT_AUTO_BACKUP);
		config.setSessionTimeout(DEFAULT_SESSION_TIMEOUT);
		config.setPasswordExpiry(DEFAULT_PASSWORD_EXPIRY);
		config.setMaximumLoginAttempts(DEFAULT_MAX_LOGIN_ATTEMPTS);

		LocalDateTime now = LocalDateTime.now();
		config.setUpdatedAt(now);
		config.setUpdatedBy(getCurrentUsername());
		config.setVersion(config.getVersion() != null ? config.getVersion() + 1 : 2);

		PlatformConfiguration restored = repository.save(config);
		recordSnapshot(restored, "RESTORE_DEFAULTS", getCurrentUsername());
		log.info("Platform configuration defaults restored successfully: id={}, version={}, updatedBy='{}'",
				restored.getId(), restored.getVersion(), getCurrentUsername());
		//logAudit(getCurrentUsername(),"CONFIGURATION_RESTORED_TO_DEFAULTS", "Completed");
		return toResponse(restored);
	}

	/**
	 * Retrieves the complete version revision history for a configuration (BR-0016,
	 * AC-0011).
	 *
	 * @param id configuration unique identifier
	 * @return list of historical snapshots ordered descending by version
	 * @throws PlatformConfigurationNotFoundException if the configuration does not
	 *                                                exist or is soft-deleted
	 */
	@Override
	public List<PlatformConfigurationHistoryResponse> getHistory(UUID id) {
		log.debug("Fetching history snapshots for configuration id: {}", id);
		findActiveConfiguration(id);

		if (historyRepository == null) {
			return List.of();
		}

		List<PlatformConfigurationHistoryResponse> historyList = historyRepository
				.findAllByConfigurationIdOrderByVersionDesc(id).stream().map(this::toHistoryResponse).toList();
		log.debug("Found {} history snapshot(s) for configuration id: {}", historyList.size(), id);
		return historyList;
	}

	/**
	 * Rolls back an active configuration to an earlier version snapshot (BR-0016,
	 * AC-0011).
	 *
	 * @param id        configuration unique identifier
	 * @param version   target version number to restore
	 * @param updatedBy user performing the rollback
	 * @return updated configuration with incremented version reflecting restored
	 *         state
	 * @throws PlatformConfigurationNotFoundException if the configuration or target
	 *                                                version does not exist
	 */
	@Override
	@Transactional
	public PlatformConfigurationResponse rollbackToVersion(UUID id, int version) {
		return rollbackToVersion(id, version, null);
	}

	@Override
	@Transactional
	public PlatformConfigurationResponse rollbackToVersion(UUID id, int version, String updatedBy) {
		log.info("Rolling back configuration id: {} to version: {}", id, version);

		PlatformConfiguration config = findActiveConfiguration(id);

		if (historyRepository == null) {
			throw new PlatformConfigurationNotFoundException("History tracking is unavailable");
		}

		PlatformConfigurationHistory snapshot = historyRepository.findByConfigurationIdAndVersion(id, version)
				.orElseThrow(() -> {
					log.warn("Target version snapshot not found: configId={}, version={}", id, version);
					return new PlatformConfigurationNotFoundException(
							"Version snapshot " + version + " not found for configuration id: " + id);
				});

		// Restore operational and platform values from snapshot
		config.setPlatformName(snapshot.getPlatformName());
		config.setPlatformUrl(snapshot.getPlatformUrl());
		config.setEnvironment(snapshot.getEnvironment());
		config.setDefaultLanguage(snapshot.getDefaultLanguage());
		config.setDefaultTimeZone(snapshot.getDefaultTimeZone());
		config.setDefaultCurrency(snapshot.getDefaultCurrency());
		config.setMaintenanceMode(snapshot.getMaintenanceMode());
		config.setFeatureToggle(snapshot.getFeatureToggle());
		config.setAutoBackup(snapshot.getAutoBackup());
		config.setSessionTimeout(snapshot.getSessionTimeout());
		config.setPasswordExpiry(snapshot.getPasswordExpiry());
		config.setMaximumLoginAttempts(snapshot.getMaximumLoginAttempts());
		config.setDescription(snapshot.getDescription());
		config.setStatus(snapshot.getStatus());
		config.setConfigurationCategory(snapshot.getConfigurationCategory());
		config.setEffectiveDate(snapshot.getEffectiveDate());

		// Pre-activation validation if restored status is ACTIVE (VAL-0012, BR-0020)
		if ("ACTIVE".equalsIgnoreCase(snapshot.getStatus())) {
			validatePreActivation(snapshot.getEnvironment(), snapshot.getAutoBackup(),
					snapshot.getSessionTimeout(), snapshot.getPasswordExpiry());
		}

		String resolvedUpdater = (updatedBy != null && !updatedBy.isBlank()) ? updatedBy : getCurrentUsername();

		// Increment version to represent new current state
		config.setVersion(config.getVersion() != null ? config.getVersion() + 1 : version + 1);
		config.setUpdatedAt(LocalDateTime.now());
		config.setUpdatedBy(resolvedUpdater);

		PlatformConfiguration rolledBack = repository.save(config);
		recordSnapshot(rolledBack, "ROLLBACK_TO_V" + version, resolvedUpdater);

		log.info("Configuration id: {} successfully rolled back to version {} (new version: {})", id, version,
				rolledBack.getVersion());
		//logAudit(resolvedUpdater,"CONFIGURATION_ROLLBACK", "Completed");
		return toResponse(rolledBack);
	}

	// =========================================================================
	// VALIDATION HELPERS
	// =========================================================================

	private void validateStatus(String status) {
		if (status == null || !ALLOWED_STATUSES.contains(status.trim().toUpperCase())) {
			log.warn("Validation failed for status: '{}'. Allowed: {}", status, ALLOWED_STATUSES);
			throw new InvalidConfigurationValueException(
					"Invalid status: '" + status + "'. Allowed values are: ACTIVE, INACTIVE");
		}
	}

	private void validateEnvironment(String environment) {
		if (environment == null || !ALLOWED_ENVIRONMENTS.contains(environment.trim().toUpperCase())) {
			log.warn("Validation failed for environment: '{}'. Allowed: {}", environment, ALLOWED_ENVIRONMENTS);
			throw new InvalidConfigurationValueException("Invalid environment: '" + environment
					+ "'. Allowed values are: DEVELOPMENT, TESTING, STAGING, PRODUCTION");
		}
	}

	private void validateToggle(String value, String fieldName) {
		if (value == null || !ALLOWED_TOGGLES.contains(value.trim().toUpperCase())) {
			log.warn("Validation failed for operational toggle '{}': '{}'. Allowed: {}", fieldName, value,
					ALLOWED_TOGGLES);
			throw new InvalidConfigurationValueException(
					"Invalid " + fieldName + ": '" + value + "'. Allowed values are: ENABLED, DISABLED");
		}
	}

	private void validateConfigurationValues(String status, String environment, String maintenanceMode,
			String featureToggle, String autoBackup) {
		validateStatus(status);
		validateEnvironment(environment);
		validateToggle(maintenanceMode, "maintenanceMode");
		validateToggle(featureToggle, "featureToggle");
		validateToggle(autoBackup, "autoBackup");
	}

	/**
	 * Validates master data fields against authoritative enterprise standards.
	 *
	 * <ul>
	 *   <li><b>Language:</b> Must adhere to ISO 639 standard two-letter codes.</li>
	 *   <li><b>Currency:</b> Must adhere to ISO 4217 standard three-letter currency codes.</li>
	 *   <li><b>Timezone:</b> Must adhere to standard IANA / ZoneId identifiers.</li>
	 *   <li><b>Category:</b> Must belong to the approved enterprise category whitelist.</li>
	 * </ul>
	 *
	 * @param category the configuration category
	 * @param language the default language code
	 * @param timezone the default time zone identifier
	 * @param currency the default currency code
	 * @throws InvalidConfigurationValueException if any master data element is invalid
	 */
	private void validateMasterData(String category, String language, String timezone, String currency) {
		if (category != null && !ALLOWED_CATEGORIES.contains(category.trim().toUpperCase())) {
			log.warn("Master data validation failed for category: '{}'. Allowed: {}", category, ALLOWED_CATEGORIES);
			throw new InvalidConfigurationValueException(
					"Invalid category: '" + category + "'. Allowed categories are: " + ALLOWED_CATEGORIES);
		}

		if (language != null && !ISO_LANGUAGES.contains(language.trim().toLowerCase())) {
			log.warn("Master data validation failed for language: '{}'", language);
			throw new InvalidConfigurationValueException(
					"Invalid language code: '" + language + "'. Must be a valid ISO 639-1 language code (e.g., 'en', 'fr', 'es').");
		}

		if (timezone != null && !IANA_TIME_ZONES.contains(timezone.trim())) {
			log.warn("Master data validation failed for timezone: '{}'", timezone);
			throw new InvalidConfigurationValueException(
					"Invalid time zone: '" + timezone + "'. Must be a valid IANA time zone identifier (e.g., 'UTC', 'America/New_York').");
		}

		if (currency != null && !ISO_CURRENCIES.contains(currency.trim().toUpperCase())) {
			log.warn("Master data validation failed for currency: '{}'", currency);
			throw new InvalidConfigurationValueException(
					"Invalid currency code: '" + currency + "'. Must be a valid ISO 4217 currency code (e.g., 'USD', 'EUR').");
		}
		//logAudit(getCurrentUsername(),"MASTER_DATA_VALIDATION", "Completed");
	}

	/**
	 * Validates business prerequisites before a configuration can be activated (VAL-0012, BR-0020).
	 *
	 * <ul>
	 *   <li>For {@code PRODUCTION} environments, automatic backups ({@code autoBackup}) must not be {@code DISABLED}.</li>
	 *   <li>The session timeout (in minutes) cannot exceed the password expiry duration (converted to minutes: {@code days * 1440}).</li>
	 * </ul>
	 *
	 * @param environment    the operational environment tier
	 * @param autoBackup     the automatic backup toggle setting
	 * @param sessionTimeout the session inactivity timeout in minutes
	 * @param passwordExpiry the password expiration period in days
	 * @throws ConfigurationActivationException if any pre-activation condition fails
	 */
	private void validatePreActivation(String environment, String autoBackup, Integer sessionTimeout, Integer passwordExpiry) {
		if ("PRODUCTION".equalsIgnoreCase(environment) && "DISABLED".equalsIgnoreCase(autoBackup)) {
			log.error("Pre-activation failed: PRODUCTION configuration cannot have Auto-Backup DISABLED");
			throw new ConfigurationActivationException("Auto-Backup DISABLED is not permitted for PRODUCTION environment");
		}

		if (sessionTimeout != null && passwordExpiry != null) {
			long passwordExpiryMinutes = (long) passwordExpiry * 24 * 60;
			if (sessionTimeout > passwordExpiryMinutes) {
				log.error("Pre-activation failed: Session timeout ({} mins) exceeds password expiry ({} mins)",
						sessionTimeout, passwordExpiryMinutes);
				throw new ConfigurationActivationException("Session timeout (" + sessionTimeout
						+ " mins) cannot exceed password expiry (" + passwordExpiryMinutes + " mins)");
			}
		}
		//logAudit(getCurrentUsername(),"PRE_ACTIVATION_VALIDATION", "Completed");
	}

	private void recordSnapshot(PlatformConfiguration config, String changeReason, String recordedBy) {
		if (historyRepository != null && config != null) {
			PlatformConfigurationHistory history = PlatformConfigurationHistory.from(config, changeReason, recordedBy);
			historyRepository.save(history);
			log.debug("Recorded version snapshot: configId={}, version={}, reason='{}'", config.getId(),
					config.getVersion(), changeReason);
		}
	}

	private PlatformConfigurationHistoryResponse toHistoryResponse(PlatformConfigurationHistory h) {
		return new PlatformConfigurationHistoryResponse(h.getId(), h.getConfigurationId(), h.getVersion(),
				h.getConfigurationName(), h.getConfigurationCategory(), h.getDescription(), h.getStatus(),
				h.getEffectiveDate(), h.getPlatformName(), h.getPlatformUrl(), h.getEnvironment(),
				h.getDefaultLanguage(), h.getDefaultTimeZone(), h.getDefaultCurrency(), h.getMaintenanceMode(),
				h.getFeatureToggle(), h.getAutoBackup(), h.getSessionTimeout(), h.getPasswordExpiry(),
				h.getMaximumLoginAttempts(), h.getRecordedAt(), h.getRecordedBy(), h.getChangeReason());
	}

	/**
	 * Looks up an active (non-soft-deleted) configuration by identifier.
	 *
	 * @param id unique identifier to search
	 * @return active {@link PlatformConfiguration} entity
	 * @throws PlatformConfigurationNotFoundException if configuration does not
	 *                                                exist or is soft-deleted
	 */
	private PlatformConfiguration findActiveConfiguration(UUID id) {
		return repository.findByIdAndDeletedFalse(id).orElseThrow(() -> {
			log.warn("Platform configuration not found with active status: id={}", id);
			return new PlatformConfigurationNotFoundException("Platform configuration not found: " + id);
		});
	}

	/**
	 * Validates that the specified configuration name is unique among active
	 * configurations on create.
	 *
	 * @param name configuration name to validate
	 * @throws DuplicateConfigurationNameException if name already exists in an
	 *                                             active configuration
	 */
	private void validateUniqueConfigurationNameForCreate(String name) {
		if (repository.existsByConfigurationNameIgnoreCaseAndDeletedFalse(name)) {
			log.warn("Configuration creation rejected: duplicate name '{}'", name);
			throw new DuplicateConfigurationNameException(name);
		}
	}

	/**
	 * Validates that the specified configuration name is unique among active
	 * configurations on update.
	 *
	 * @param name      updated configuration name
	 * @param currentId id of the configuration being updated
	 * @throws DuplicateConfigurationNameException if another active configuration
	 *                                             already uses this name
	 */
	private void validateUniqueConfigurationNameForUpdate(String name, UUID currentId) {
		if (repository.existsByConfigurationNameIgnoreCaseAndDeletedFalseAndIdNot(name, currentId)) {
			log.warn("Configuration update rejected: duplicate name '{}' used by configuration id={}", name, currentId);
			throw new DuplicateConfigurationNameException(name);
		}
	}

	/**
	 * Maps an internal entity to the public response DTO.
	 *
	 * @param config source entity
	 * @return populated {@link PlatformConfigurationResponse} DTO
	 */
	private PlatformConfigurationResponse toResponse(PlatformConfiguration config) {
		return new PlatformConfigurationResponse(config.getId(), config.getConfigurationName(),
				config.getConfigurationCategory(), config.getDescription(), config.getStatus(),
				config.getEffectiveDate(), config.getPlatformName(), config.getPlatformUrl(), config.getEnvironment(),
				config.getDefaultLanguage(), config.getDefaultTimeZone(), config.getDefaultCurrency(),
				config.getMaintenanceMode(), config.getFeatureToggle(), config.getAutoBackup(),
				config.getSessionTimeout(), config.getPasswordExpiry(), config.getMaximumLoginAttempts(),
				config.getCreatedAt(), config.getCreatedBy(), config.getUpdatedAt(), config.getUpdatedBy(),
				config.getVersion() != null ? config.getVersion() : 1);
	}

	/**
	 * Resolves the username of the currently authenticated principal from
	 * {@link SecurityContextHolder}. Falls back to "admin" if no authenticated
	 * principal is found.
	 *
	 * @return current authenticated username or "admin"
	 */
	private String getCurrentUsername() {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
	        Object principal = auth.getPrincipal();
	        // 1. Reconstructed JWT Principal from CommonJwtAuthenticationFilter
	        if (principal instanceof JwtUserPrincipal jwtUser) {
	            String username = jwtUser.getUsername();
	            if (username != null && !username.isBlank()) {
	                log.debug("Resolved authenticated user from JwtUserPrincipal: '{}'", username);
	                return username.trim();
	            }
	        }
	        // 2. Standard Spring UserDetails
	        if (principal instanceof UserDetails userDetails) {
	            String username = userDetails.getUsername();
	            if (username != null && !username.isBlank()) {
	                log.debug("Resolved authenticated user from UserDetails: '{}'", username);
	                return username.trim();
	            }
	        }
	        // 3. String principal or Authentication name
	        String name = auth.getName();
	        if (name != null && !name.isBlank()) {
	            log.debug("Resolved authenticated user from Authentication.getName(): '{}'", name.trim());
	            return name.trim();
	        }
	    }
	    log.debug("No authenticated user in SecurityContext, falling back to: 'admin'");
	    return "admin";
	}

	private String resolveUser(String requestUser, String fallback) {
		if (requestUser != null && !requestUser.isBlank()) {
			log.debug("Resolved user from request parameter: '{}'", requestUser.trim());
			return requestUser.trim();
		}
		return getCurrentUsername();
	}
	
	/*
	 * private void logAudit(String user, String action, String status) { if
	 * (this.auditServiceClient != null) { try { auditServiceClient.logEvent(user,
	 * "CONFIGURATION_CREATED", "SUCCESS"); } catch (Exception ex) {
	 * log.warn("Failed to record audit log: {}", ex.getMessage()); } } }
	 */
}