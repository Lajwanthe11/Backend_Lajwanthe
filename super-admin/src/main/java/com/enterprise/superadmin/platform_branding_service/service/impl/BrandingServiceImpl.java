package com.enterprise.superadmin.platform_branding_service.service.impl;

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
import com.enterprise.superadmin.platform_branding_service.service.BrandingService;
import com.enterprise.superadmin.platform_branding_service.service.BrandingValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of the Platform Branding service.
 *
 * <p>Persistence strategy:</p>
 * <ul>
 *     <li>Reuses the existing configurations table.</li>
 *     <li>Branding configuration is identified by:
 *         category = BRANDING and scope = PLATFORM.</li>
 *     <li>Each branding property is stored as an individual
 *         configuration row.</li>
 * </ul>
 *
 * <p>Audit strategy:</p>
 * <ul>
 *     <li>Administrative branding actions are sent through
 *         AuditLogClient.</li>
 *     <li>The branding module does not directly access audit_logs.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class BrandingServiceImpl implements BrandingService {

    private static final Logger log = LoggerFactory.getLogger(BrandingServiceImpl.class);

    // =====================================================================
    // CONFIGURATION CONTEXT
    // =====================================================================

    private static final String CATEGORY = "BRANDING";
    private static final String SCOPE = "PLATFORM";
    private static final String CONFIG_STATUS_ACTIVE = "ACTIVE";

    // =====================================================================
    // BRANDING LIFECYCLE STATUS
    // =====================================================================

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    // =====================================================================
    // AUDIT CONTEXT
    // =====================================================================

    private static final String AUDIT_MODULE = "PLATFORM_BRANDING";
    private static final String AUDIT_ENTITY = "PLATFORM_BRANDING";
    private static final String AUDIT_ENTITY_ID = "PLATFORM";
    private static final String AUDIT_SUCCESS = "SUCCESS";

    private static final String ACTION_CREATED = "BRANDING_CREATED";
    private static final String ACTION_UPDATED = "BRANDING_UPDATED";
    private static final String ACTION_RESET = "BRANDING_RESET";
    private static final String ACTION_PUBLISHED = "BRANDING_PUBLISHED";

    // =====================================================================
    // BRANDING CONFIGURATION KEYS
    // =====================================================================

    private static final String KEY_PLATFORM_NAME = "branding.platform_name";
    private static final String KEY_COMPANY_NAME = "branding.company_name";
    private static final String KEY_TAGLINE = "branding.tagline";
    private static final String KEY_LOGO_URL = "branding.logo_url";
    private static final String KEY_LOGIN_BACKGROUND_URL = "branding.login_background_url";
    private static final String KEY_WELCOME_MESSAGE = "branding.welcome_message";
    private static final String KEY_PRIMARY_COLOR = "branding.primary_color";
    private static final String KEY_SECONDARY_COLOR = "branding.secondary_color";
    private static final String KEY_ACCENT_COLOR = "branding.accent_color";
    private static final String KEY_THEME = "branding.theme";
    private static final String KEY_FAVICON_URL = "branding.favicon_url";
    private static final String KEY_EMAIL_HEADER_LOGO_URL = "branding.email_header_logo_url";
    private static final String KEY_FOOTER_TEXT = "branding.footer_text";
    private static final String KEY_COPYRIGHT_TEXT = "branding.copyright_text";
    private static final String KEY_STATUS = "branding.status";

    // =====================================================================
    // DEPENDENCIES
    // =====================================================================

    private final ConfigurationRepository configurationRepository;
    private final BrandingValidationService validationService;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogClient auditLogClient;

    // =====================================================================
    // CONSTRUCTOR
    // =====================================================================

    public BrandingServiceImpl(
            ConfigurationRepository configurationRepository,
            BrandingValidationService validationService,
            CurrentUserProvider currentUserProvider,
            AuditLogClient auditLogClient) {

        this.configurationRepository = configurationRepository;
        this.validationService = validationService;
        this.currentUserProvider = currentUserProvider;
        this.auditLogClient = auditLogClient;
    }

    // =====================================================================
    // GET CURRENT BRANDING
    // =====================================================================

    @Override
    public BrandingResponse getCurrentBranding() {

        log.info("Fetching current platform branding");

        BrandingResponse response = buildResponse();

        log.info("Platform branding fetched successfully. status={}", response.getStatus());

        return response;
    }

    // =====================================================================
    // CREATE / INITIALIZE BRANDING
    // =====================================================================

    @Override
    @Transactional
    public BrandingResponse createBranding(BrandingCreateRequest request) {

        log.info("Starting platform branding initialization");

        validateCreateRequest(request);

        Map<String, String> existingBranding = loadBrandingValues();

        if (!existingBranding.isEmpty()) {
            throw new BrandingAlreadyExistsException("Platform branding configuration already exists");
        }

        String actor = getCurrentActor();

        save(KEY_PLATFORM_NAME, normalize(request.getPlatformName()), actor);
        save(KEY_COMPANY_NAME, normalize(request.getCompanyName()), actor);
        save(KEY_TAGLINE, normalize(request.getTagline()), actor);
        save(KEY_LOGO_URL, normalize(request.getLogoUrl()), actor);
        save(KEY_LOGIN_BACKGROUND_URL, normalize(request.getLoginBackgroundUrl()), actor);
        save(KEY_WELCOME_MESSAGE, normalize(request.getWelcomeMessage()), actor);
        save(KEY_PRIMARY_COLOR, normalize(request.getPrimaryColor()), actor);
        save(KEY_SECONDARY_COLOR, normalize(request.getSecondaryColor()), actor);
        save(KEY_ACCENT_COLOR, normalize(request.getAccentColor()), actor);
        save(KEY_THEME, normalizeTheme(request.getTheme()), actor);
        save(KEY_FAVICON_URL, normalize(request.getFaviconUrl()), actor);
        save(KEY_EMAIL_HEADER_LOGO_URL, normalize(request.getEmailHeaderLogoUrl()), actor);
        save(KEY_FOOTER_TEXT, normalize(request.getFooterText()), actor);
        save(KEY_COPYRIGHT_TEXT, normalize(request.getCopyrightText()), actor);
        save(KEY_STATUS, STATUS_DRAFT, actor);
        recordAudit(ACTION_CREATED, actor);
        log.info("Platform branding initialization completed successfully");

        return buildResponse();
    }

    // =====================================================================
    // UPDATE BRANDING
    // =====================================================================

    @Override
    @Transactional
    public BrandingResponse updateBranding(BrandingUpdateRequest request) {

        log.info("Starting platform branding update");

        validateUpdateRequest(request);

        Map<String, String> existingBranding = loadBrandingValues();

        if (existingBranding.isEmpty()) {
            throw new BrandingNotFoundException("Platform branding configuration not found");
        }

        String actor = getCurrentActor();

        updateIfPresent(KEY_PLATFORM_NAME, request.getPlatformName(), actor);
        updateIfPresent(KEY_COMPANY_NAME, request.getCompanyName(), actor);
        updateIfPresent(KEY_TAGLINE, request.getTagline(), actor);
        updateIfPresent(KEY_LOGO_URL, request.getLogoUrl(), actor);
        updateIfPresent(KEY_LOGIN_BACKGROUND_URL, request.getLoginBackgroundUrl(), actor);
        updateIfPresent(KEY_WELCOME_MESSAGE, request.getWelcomeMessage(), actor);
        updateIfPresent(KEY_PRIMARY_COLOR, request.getPrimaryColor(), actor);
        updateIfPresent(KEY_SECONDARY_COLOR, request.getSecondaryColor(), actor);
        updateIfPresent(KEY_ACCENT_COLOR, request.getAccentColor(), actor);
        updateIfPresent(KEY_THEME, normalizeTheme(request.getTheme()), actor);
        updateIfPresent(KEY_FAVICON_URL, request.getFaviconUrl(), actor);
        updateIfPresent(KEY_EMAIL_HEADER_LOGO_URL, request.getEmailHeaderLogoUrl(), actor);
        updateIfPresent(KEY_FOOTER_TEXT, request.getFooterText(), actor);
        updateIfPresent(KEY_COPYRIGHT_TEXT, request.getCopyrightText(), actor);

        recordAudit(ACTION_UPDATED, actor);

        log.info("Platform branding update completed successfully");
        return buildResponse();
    }

    // =====================================================================
    // RESET BRANDING
    // =====================================================================

    @Override
    @Transactional
    public BrandingResponse resetBranding() {

        log.info("Starting platform branding reset");

        Map<String, String> existingBranding = loadBrandingValues();

        if (existingBranding.isEmpty()) {
            throw new BrandingNotFoundException("Platform branding configuration not found");
        }

        String actor = getCurrentActor();

        /*
         * Current application defaults.
         *
         * Final product defaults should be confirmed before
         * production release.
         */

        save(KEY_PLATFORM_NAME, "Platform", actor);
        save(KEY_COMPANY_NAME, null, actor);
        save(KEY_TAGLINE, null, actor);
        save(KEY_LOGO_URL, null, actor);
        save(KEY_LOGIN_BACKGROUND_URL, null, actor);
        save(KEY_WELCOME_MESSAGE, null, actor);
        save(KEY_PRIMARY_COLOR, "#0052CC", actor);
        save(KEY_SECONDARY_COLOR, "#172B4D", actor);
        save(KEY_ACCENT_COLOR, null, actor);
        save(KEY_THEME, "LIGHT", actor);
        save(KEY_FAVICON_URL, null, actor);
        save(KEY_EMAIL_HEADER_LOGO_URL, null, actor);
        save(KEY_FOOTER_TEXT, null, actor);
        save(KEY_COPYRIGHT_TEXT, null, actor);
        save(KEY_STATUS, STATUS_DRAFT, actor);
        recordAudit(ACTION_RESET, actor);

        log.info("Platform branding reset completed successfully");

        return buildResponse();
    }

    // =====================================================================
    // PREVIEW BRANDING
    // =====================================================================

    @Override
    public BrandingResponse previewBranding(BrandingUpdateRequest request) {

        log.info("Generating platform branding preview");

        validateUpdateRequest(request);

        BrandingResponse current = buildResponse();

        BrandingResponse preview = new BrandingResponse(
                        choose(request.getPlatformName(), current.getPlatformName()),
                        choose(request.getCompanyName(), current.getCompanyName()),
                        choose(request.getTagline(), current.getTagline()),
                        choose(request.getLogoUrl(), current.getLogoUrl()),
                        choose(request.getLoginBackgroundUrl(), current.getLoginBackgroundUrl()),
                        choose(request.getWelcomeMessage(), current.getWelcomeMessage()),
                        choose(request.getPrimaryColor(), current.getPrimaryColor()),
                        choose(request.getSecondaryColor(), current.getSecondaryColor()),
                        choose(request.getAccentColor(), current.getAccentColor()),
                        choose(normalizeTheme(request.getTheme()), current.getTheme()),
                        choose(request.getFaviconUrl(), current.getFaviconUrl()),
                        choose(request.getEmailHeaderLogoUrl(), current.getEmailHeaderLogoUrl()),
                        choose(request.getFooterText(), current.getFooterText()),
                        choose(request.getCopyrightText(), current.getCopyrightText()),
                        current.getStatus()
                );

        log.info("Platform branding preview generated successfully");

        return preview;
    }

    // =====================================================================
    // PUBLISH BRANDING
    // =====================================================================

    @Override
    @Transactional
    public BrandingResponse publishBranding() {

        log.info("Publishing platform branding");

        BrandingResponse current = buildResponse();

        if (!StringUtils.hasText(current.getStatus())) {
            throw new BrandingNotFoundException("Platform branding configuration not found");
        }

        if (STATUS_PUBLISHED.equalsIgnoreCase(current.getStatus())) {
            throw new BrandingInvalidStateException("Platform branding is already published");
        }

        if (!STATUS_DRAFT.equalsIgnoreCase(current.getStatus())) {
            throw new BrandingInvalidStateException("Platform branding cannot be published from status: " + current.getStatus());
        }

        String actor = getCurrentActor();

        save(KEY_STATUS, STATUS_PUBLISHED, actor);

        recordAudit(ACTION_PUBLISHED, actor);
        log.info("Platform branding published successfully");

        return buildResponse();
    }

    // =====================================================================
    // CREATE VALIDATION
    // =====================================================================

    private void validateCreateRequest(BrandingCreateRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Branding create request must not be null");
        }

        validationService.validatePlatformName(request.getPlatformName());
        validationService.validateCompanyName(request.getCompanyName());
        validationService.validateWelcomeMessage(request.getWelcomeMessage());
        validationService.validateFooterText(request.getFooterText());
        validationService.validateCopyrightText(request.getCopyrightText());
    }

    // =====================================================================
    // UPDATE VALIDATION
    // =====================================================================

    private void validateUpdateRequest(
            BrandingUpdateRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Branding update request must not be null");
        }

        if (request.getPlatformName() != null) {
            validationService.validatePlatformName(request.getPlatformName());
        }

        if (request.getCompanyName() != null) {
            validationService.validateCompanyName(request.getCompanyName());
        }

        if (request.getWelcomeMessage() != null) {
            validationService.validateWelcomeMessage(request.getWelcomeMessage());
        }

        if (request.getFooterText() != null) {
            validationService.validateFooterText(request.getFooterText());
        }

        if (request.getCopyrightText() != null) {
            validationService.validateCopyrightText(request.getCopyrightText());
        }

        if (request.getPrimaryColor() != null) {
            validationService.validateHexColor("primary_color", request.getPrimaryColor());
        }

        if (request.getSecondaryColor() != null) {
            validationService.validateHexColor("secondary_color", request.getSecondaryColor());
        }

        if (request.getAccentColor() != null) {
            validationService.validateHexColor("accent_color", request.getAccentColor());
        }

        if (request.getTheme() != null) {
            validationService.validateTheme(request.getTheme());
        }
    }

    // =====================================================================
    // PERSISTENCE
    // =====================================================================

    private void updateIfPresent(String key, String value, String actor) {

        if (value == null) {
            return;
        }
        save(key, normalize(value), actor);
    }

    private void save(String key, String value, String actor) {

        Optional<Configuration> existing =
                configurationRepository.findByConfigKeyAndCategoryAndScopeAndIsDeletedFalse(key, CATEGORY, SCOPE);

        Configuration configuration = existing.orElseGet(Configuration::new);

        boolean newConfiguration = existing.isEmpty();

        configuration.setConfigKey(key);
        configuration.setConfigValue(value);
        configuration.setCategory(CATEGORY);
        configuration.setScope(SCOPE);
        configuration.setStatus(CONFIG_STATUS_ACTIVE);
        configuration.setIsDeleted(false);

        LocalDate today = LocalDate.now();

        configuration.setUpdatedAt(today);
        configuration.setUpdatedBy(actor);

        if (newConfiguration) {
            configuration.setCreatedAt(today);
            configuration.setCreatedBy(actor);
        }

        configurationRepository.save(configuration);
    }

    // =====================================================================
    // RESPONSE MAPPING
    // =====================================================================

    /**
     * Loads all active branding configurations in one database query.
     */
    private BrandingResponse buildResponse() {

        Map<String, String> brandingValues = loadBrandingValues();

        return new BrandingResponse(
                brandingValues.get(KEY_PLATFORM_NAME),
                brandingValues.get(KEY_COMPANY_NAME),
                brandingValues.get(KEY_TAGLINE),
                brandingValues.get(KEY_LOGO_URL),
                brandingValues.get(KEY_LOGIN_BACKGROUND_URL),
                brandingValues.get(KEY_WELCOME_MESSAGE),
                brandingValues.get(KEY_PRIMARY_COLOR),
                brandingValues.get(KEY_SECONDARY_COLOR),
                brandingValues.get(KEY_ACCENT_COLOR),
                brandingValues.get(KEY_THEME),
                brandingValues.get(KEY_FAVICON_URL),
                brandingValues.get(KEY_EMAIL_HEADER_LOGO_URL),
                brandingValues.get(KEY_FOOTER_TEXT),
                brandingValues.get(KEY_COPYRIGHT_TEXT),
                brandingValues.get(KEY_STATUS)
        );
    }

    /**
     * Retrieves all active PLATFORM branding configurations
     * using a single database query.
     */
    private Map<String, String> loadBrandingValues() {

        log.debug("Loading platform branding configurations. " + "category={}, scope={}", CATEGORY, SCOPE);

        List<Configuration> configurations =
                configurationRepository.findByCategoryAndScopeAndIsDeletedFalse(CATEGORY, SCOPE);

        Map<String, String> brandingValues = new HashMap<>(configurations.size());

        for (Configuration configuration : configurations) {

            if (configuration.getConfigKey() == null) {
                continue;
            }
            brandingValues.put(configuration.getConfigKey(), configuration.getConfigValue());
        }

        log.debug("Loaded {} platform branding configuration values", brandingValues.size());

        return brandingValues;
    }

    // =====================================================================
    // AUDIT
    // =====================================================================

    /**
     * Records a successful branding administrative action through
     * the approved audit integration contract.
     */
    private void recordAudit(String action, String actorId) {

        auditLogClient.record(
                actorId,
                action,
                AUDIT_MODULE,
                AUDIT_ENTITY,
                AUDIT_ENTITY_ID,
                AUDIT_SUCCESS,
                LocalDateTime.now()
        );

        log.debug("Branding audit recorded. action={}, actor={}", action, actorId);
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private String normalize(String value) {

        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String normalizeTheme(String theme) {

        if (!StringUtils.hasText(theme)) {
            return null;
        }

        return theme.trim().toUpperCase();
    }

    private String choose(String requestedValue, String currentValue) {
        return requestedValue != null ? normalize(requestedValue) : currentValue;
    }

    /**
     * Resolves the currently authenticated actor.
     */
    private String getCurrentActor() {

        String actor = currentUserProvider.getCurrentUserId();

        if (!StringUtils.hasText(actor)) {
            throw new IllegalStateException("Authenticated user ID is not available");
        }

        return actor.trim();
    }
}