package com.enterprise.superadmin.platform_branding_service.integration.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
@Profile("dev")
public class LocalStorageClient implements StorageClient {

    private final Path rootDirectory = Paths.get("uploads", "branding");

    public LocalStorageClient() throws IOException {
        Files.createDirectories(rootDirectory);
    }

    @Override
    public String upload(MultipartFile file, String folder) throws IOException {

        Path directory = rootDirectory.resolve(folder);
        Files.createDirectories(directory);
        String originalFilename = file.getOriginalFilename();

        String filename = UUID.randomUUID() + "-" + (originalFilename == null ? "file" : originalFilename);

        Path target = directory.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return target.toString();
    }

    @Override
    public String replace(String existingReference, MultipartFile file, String folder) throws IOException {

        if (existingReference != null && !existingReference.isBlank()) {
            Files.deleteIfExists(Paths.get(existingReference));
        }

        return upload(file, folder);
    }

    @Override
    public void delete(String reference) throws IOException {

        if (reference == null || reference.isBlank()) {
            return;
        }
        Files.deleteIfExists(Paths.get(reference));
    }
}