package com.example.platformadmin.superadmin.platform_branding_service.integration.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalStorageClient implements StorageClient {

    private final Path storageRoot;

    public LocalStorageClient() throws IOException {
        this.storageRoot = Path.of("storage");
        Files.createDirectories(storageRoot);
    }

    @Override
    public String upload(org.springframework.web.multipart.MultipartFile file, String folder) {
        // existing implementation
        return "";
    }

    @Override
    public String replace(String existingReference,
                           org.springframework.web.multipart.MultipartFile file,
                           String folder) {
        // existing implementation
        return "";
    }

    @Override
    public void delete(String reference) {
        // existing implementation
    }
}