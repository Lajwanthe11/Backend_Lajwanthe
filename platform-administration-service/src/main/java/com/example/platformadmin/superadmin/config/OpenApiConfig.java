package com.example.platformadmin.superadmin.config;

import io.swagger.v3.oas.models.Components;
<<<<<<< HEAD

=======
>>>>>>> 966179ecaf2c77d5a11eb64efedb43b3d950f6eb
import io.swagger.v3.oas.models.OpenAPI;

import io.swagger.v3.oas.models.info.Info;
<<<<<<< HEAD

import io.swagger.v3.oas.models.security.SecurityRequirement;

=======
import io.swagger.v3.oas.models.security.SecurityRequirement;
>>>>>>> 966179ecaf2c77d5a11eb64efedb43b3d950f6eb
import io.swagger.v3.oas.models.security.SecurityScheme;

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
<<<<<<< HEAD

                        .description("Unified API documentation for Super Admin Management"))

                // Applies Bearer Auth across all endpoints in Swagger UI:

                .addSecurityItem(new SecurityRequirement().addList( "bearerAuth"))

                // Defines the Bearer Auth input modal in Swagger UI:

                .components(new Components()

                        .addSecuritySchemes( "bearerAuth", new SecurityScheme()

                                .name( "bearerAuth")

                                .type(SecurityScheme.Type.HTTP)

                                .scheme("bearer")

                                .bearerFormat("JWT")

                                .description("Paste your accessToken obtained from auth-service")));

=======
                        .description("Unified API documentation for Super Admin Management"))
                // Applies Bearer Auth across all endpoints in Swagger UI:
                    .addSecurityItem(new SecurityRequirement().addList( "bearerAuth"))
                    // Defines the Bearer Auth input modal in Swagger UI:
                    .components(new Components()
                            .addSecuritySchemes( "bearerAuth", new SecurityScheme()
                                    .name( "bearerAuth")
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                                    .description("Paste your accessToken obtained from auth-service")));
>>>>>>> 966179ecaf2c77d5a11eb64efedb43b3d950f6eb
    }

}
