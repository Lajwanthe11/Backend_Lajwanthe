package com.example.platformadmin.superadmin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI superAdminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Super Admin Management API")
                        .version("v1.0")
                        .description("Unified API documentation for Super Admin Management"));
    }
}