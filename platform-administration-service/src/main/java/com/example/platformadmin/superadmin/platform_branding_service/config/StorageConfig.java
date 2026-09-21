package com.example.platformadmin.superadmin.platform_branding_service.config;

import com.example.platformadmin.superadmin.platform_branding_service.integration.storage.LocalStorageClient;
import com.example.platformadmin.superadmin.platform_branding_service.integration.storage.StorageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class StorageConfig {

    @Bean
    public StorageClient storageClient() throws IOException {
        return new LocalStorageClient();
    }
}