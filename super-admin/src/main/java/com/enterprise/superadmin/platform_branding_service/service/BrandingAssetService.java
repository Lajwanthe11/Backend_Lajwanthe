package com.enterprise.superadmin.platform_branding_service.service;

import com.enterprise.superadmin.platform_branding_service.integration.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * Handles branding asset validation and storage orchestration.
 *
 * Supported branding assets:
 * - Company logo
 * - Login background
 * - Favicon
 * - Email header logo
 *
 * Responsibilities:
 * - Validate asset presence
 * - Validate file extension
 * - Validate MIME type
 * - Validate file size
 * - Upload assets
 * - Replace assets
 * - Delete assets
 *
 * Storage-provider-specific logic belongs in StorageClient.
 */
@Service
public class BrandingAssetService {

    private static final Logger log = LoggerFactory.getLogger(BrandingAssetService.class);

    // ---------------------------------------------------------------------
    // Maximum file sizes
    // ---------------------------------------------------------------------

    private static final long MAX_LOGO_SIZE = 5L * 1024 * 1024;
    private static final long MAX_LOGIN_BACKGROUND_SIZE = 10L * 1024 * 1024;

    /*
     * Favicon and email-header-logo limits are not explicitly defined
     * in the supplied branding requirements. Until approved limits exist,
     * only extension/MIME validation is applied to those asset types.
     */

    // ---------------------------------------------------------------------
    // Supported file extensions
    // ---------------------------------------------------------------------

    private static final Set<String> LOGO_EXTENSIONS = Set.of("png", "jpg", "jpeg", "svg");
    private static final Set<String> BACKGROUND_EXTENSIONS = Set.of("png", "jpg", "jpeg");
    private static final Set<String> FAVICON_EXTENSIONS = Set.of("png", "jpg", "jpeg", "ico", "svg");
    private static final Set<String> EMAIL_LOGO_EXTENSIONS = Set.of("png", "jpg", "jpeg", "svg");

    // ---------------------------------------------------------------------
    // Supported MIME types
    // ---------------------------------------------------------------------

    private static final Set<String> IMAGE_MIME_TYPES = Set.of("image/png", "image/jpeg");
    private static final Set<String> LOGO_MIME_TYPES = Set.of("image/png", "image/jpeg", "image/svg+xml");
    private static final Set<String> FAVICON_MIME_TYPES = Set.of("image/png", "image/jpeg", "image/x-icon", "image/vnd.microsoft.icon", "image/svg+xml");
    private final StorageClient storageClient;

    public BrandingAssetService(StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    // =====================================================================
    // COMPANY LOGO
    // =====================================================================

    /**
     * Upload a company logo.
     */

    public String uploadLogo(MultipartFile file) {

        log.info("Starting company logo upload");

        validateFilePresent(file, "logo");
        validateExtension(file, LOGO_EXTENSIONS, "logo");
        validateMimeType(file, LOGO_MIME_TYPES, "logo");
        validateMaxSize(file, MAX_LOGO_SIZE, "logo");

        return upload(file, "branding/logo", "logo");
    }

    /**
     * Replace the existing company logo.
     */
    public String replaceLogo(String existingReference, MultipartFile file) {

        log.info("Starting company logo replacement");

        validateFilePresent(file, "logo");
        validateExtension(file, LOGO_EXTENSIONS, "logo");
        validateMimeType(file, LOGO_MIME_TYPES, "logo");
        validateMaxSize(file, MAX_LOGO_SIZE, "logo");

        return replace(existingReference, file, "branding/logo", "logo");
    }

    /**
     * Remove an existing company logo.
     */
    public void deleteLogo(String reference) {
        delete(reference, "logo");
    }

    // =====================================================================
    // LOGIN BACKGROUND
    // =====================================================================

    /**
     * Upload login background image.
     */
    public String uploadLoginBackground(MultipartFile file) {

        log.info("Starting login background upload");

        validateFilePresent(file, "login background");
        validateExtension(file, BACKGROUND_EXTENSIONS, "login background");
        validateMimeType(file, IMAGE_MIME_TYPES, "login background");
        validateMaxSize(file, MAX_LOGIN_BACKGROUND_SIZE, "login background");

        return upload(file, "branding/login-background", "login background");
    }

    /**
     * Replace login background image.
     */
    public String replaceLoginBackground(String existingReference, MultipartFile file) {

        log.info("Starting login background replacement");
        validateFilePresent(file, "login background");
        validateExtension(file, BACKGROUND_EXTENSIONS, "login background");
        validateMimeType(file, IMAGE_MIME_TYPES, "login background");
        validateMaxSize(file, MAX_LOGIN_BACKGROUND_SIZE, "login background");

        return replace(existingReference, file, "branding/login-background", "login background");
    }

    /**
     * Remove login background image.
     */

    public void deleteLoginBackground(String reference) {
        delete(reference, "login background");
    }

    // =====================================================================
    // FAVICON
    // =====================================================================

    /**
     * Upload favicon.
     */
    public String uploadFavicon(MultipartFile file) {

        log.info("Starting favicon upload");

        validateFilePresent(file, "favicon");
        validateExtension(file, FAVICON_EXTENSIONS, "favicon");
        validateMimeType(file, FAVICON_MIME_TYPES, "favicon");

        return upload(file, "branding/favicon", "favicon");
    }

    /**
     * Replace favicon.
     */
    public String replaceFavicon(String existingReference, MultipartFile file) {

        log.info("Starting favicon replacement");

        validateFilePresent(file, "favicon");
        validateExtension(file, FAVICON_EXTENSIONS, "favicon");
        validateMimeType(file, FAVICON_MIME_TYPES, "favicon");

        return replace(existingReference, file, "branding/favicon", "favicon");
    }

    /**
     * Remove favicon.
     */
    public void deleteFavicon(String reference) {
        delete(reference, "favicon");
    }

    // =====================================================================
    // EMAIL HEADER LOGO
    // =====================================================================

    /**
     * Upload email header logo.
     */
    public String uploadEmailHeaderLogo(MultipartFile file) {

        log.info("Starting email header logo upload");

        validateFilePresent(file, "email header logo");
        validateExtension(file, EMAIL_LOGO_EXTENSIONS, "email header logo");
        validateMimeType(file, LOGO_MIME_TYPES, "email header logo");

        return upload(file, "branding/email-header-logo", "email header logo");
    }

    /**
     * Replace email header logo.
     */
    public String replaceEmailHeaderLogo(String existingReference, MultipartFile file) {

        log.info("Starting email header logo replacement");

        validateFilePresent(file, "email header logo");
        validateExtension(file, EMAIL_LOGO_EXTENSIONS, "email header logo");
        validateMimeType(file, LOGO_MIME_TYPES, "email header logo");

        return replace(existingReference, file, "branding/email-header-logo", "email header logo");
    }

    /**
     * Remove email header logo.
     */
    public void deleteEmailHeaderLogo(String reference) {
        delete(reference, "email header logo");
    }

    // =====================================================================
    // COMMON STORAGE OPERATIONS
    // =====================================================================

    private String upload(MultipartFile file, String folder, String assetName) {

        try {
            String reference = storageClient.upload(file, folder);
            log.info("{} upload completed successfully", assetName);
            return reference;

        } catch (IOException exception) {

            log.error("Storage upload failed for asset={}", assetName, exception);
            throw new IllegalStateException("Failed to store " + assetName, exception);
        }
    }

    private String replace(String existingReference, MultipartFile file, String folder, String assetName) {

        if (!StringUtils.hasText(existingReference)) {
            /*
             * There is no existing asset to replace.
             * Treat the operation as a normal upload.
             */
            log.info("No existing {} reference found. Performing upload instead", assetName);
            return upload(file, folder, assetName);
        }

        try {

            String reference = storageClient.replace(existingReference, file, folder);
            log.info("{} replacement completed successfully", assetName);

            return reference;

        } catch (IOException exception) {

            log.error("Storage replacement failed for asset={}", assetName, exception);
            throw new IllegalStateException("Failed to replace " + assetName, exception
            );
        }
    }

    private void delete(String reference, String assetName) {

        if (!StringUtils.hasText(reference)) {
            log.debug("Skipping {} deletion because reference is empty", assetName);
            return;
        }
        try {
            storageClient.delete(reference);
            log.info("{} deleted successfully", assetName);

        } catch (IOException exception) {
            log.error("Storage deletion failed for asset={}", assetName, exception);
            throw new IllegalStateException("Failed to delete " + assetName, exception
            );
        }
    }

    // =====================================================================
    // VALIDATION
    // =====================================================================

    private void validateFilePresent(MultipartFile file, String assetName) {

        if (file == null || file.isEmpty()) {
            log.warn("Branding asset validation failed: {} file is empty", assetName);
            throw new IllegalArgumentException(assetName + " file must not be empty");
        }
    }

    private void validateExtension(MultipartFile file, Set<String> allowedExtensions, String assetName) {

        String originalFilename = file.getOriginalFilename();

        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException(assetName + " filename must be provided");
        }

        int lastDot = originalFilename.lastIndexOf('.');

        if (lastDot < 0 || lastDot == originalFilename.length() - 1) {
            log.warn("Branding asset validation failed: missing extension for {}", assetName);
            throw new IllegalArgumentException(assetName + " must have a supported file extension");
        }

        String extension = originalFilename.substring(lastDot + 1).toLowerCase(Locale.ROOT);

        if (!allowedExtensions.contains(extension)) {
            log.warn("Branding asset validation failed: unsupported extension for {}", assetName);
            throw new IllegalArgumentException(assetName + " has an unsupported file type");
        }
    }

    private void validateMimeType(MultipartFile file, Set<String> allowedMimeTypes, String assetName) {

        String contentType = file.getContentType();

        if (!StringUtils.hasText(contentType) || !allowedMimeTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
            log.warn("Branding asset validation failed: unsupported MIME type for {}", assetName);
            throw new IllegalArgumentException(assetName + " has an unsupported content type");
        }
    }

    private void validateMaxSize(MultipartFile file, long maxSize, String assetName) {

        if (file.getSize() > maxSize) {
            log.warn("Branding asset validation failed: {} exceeds maximum allowed size", assetName);
            throw new IllegalArgumentException(assetName + " exceeds the maximum allowed file size");
        }
    }
}