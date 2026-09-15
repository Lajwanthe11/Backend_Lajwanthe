package com.example.platformadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Platform Administration Service.
 * Manages organizations, companies, departments, and business units.
 */
@SpringBootApplication(scanBasePackages = {"com.example.platformadmin", "com.example.common"})
public class PlatformAdministrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformAdministrationApplication.class, args);
    }
}
