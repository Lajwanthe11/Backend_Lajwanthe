package com.example.platformadmin.superadmin.platform_branding_service.integration.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Abstraction for platform branding asset storage.
 *
 * The branding domain must not depend directly on a specific
 * storage provider.
 */
public interface StorageClient {

    /**
     * Uploads a new asset and returns its storage reference.
     *
     * @param file asset to upload
     * @param folder logical storage folder
     * @return storage object key or URL
     */
    String upload(MultipartFile file, String folde) throws IOException;

    /**
     * Replaces an existing asset.
     *
     * @param existingReference existing storage object reference
     * @param file new asset
     * @param folder logical storage folder
     * @return new storage object key or URL
     */
    String replace(String existingReference, MultipartFile file, String folder) throws IOException;

    /**
     * Removes an asset from storage.
     *
     * @param reference storage object key or URL
     */
    void delete(
            String reference
    ) throws IOException;
}