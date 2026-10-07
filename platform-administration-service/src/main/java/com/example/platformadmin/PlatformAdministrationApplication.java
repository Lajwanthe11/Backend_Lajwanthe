package com.example.platformadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Platform Administration Service.
 * Manages organizations, companies, departments, and business units.
 */
<<<<<<< HEAD
@SpringBootApplication(scanBasePackages = { "com.example.platformadmin", "com.example.common" })
=======
@SpringBootApplication(scanBasePackages = {"com.example.platformadmin", "com.example.common"})
@EnableScheduling
>>>>>>> f2806f67474dcf34c5bffa52b9c4078e92c712be
public class PlatformAdministrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformAdministrationApplication.class, args);
    }
}
