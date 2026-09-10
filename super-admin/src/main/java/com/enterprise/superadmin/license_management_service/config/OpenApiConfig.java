package com.enterprise.superadmin.license_management_service.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI licenseManagementOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("License Management Service")
                        .description("Panther - Super Admin License Management APIs")
                        .version("1.0.0"))
                .schemaRequirement(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }
}