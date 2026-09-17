package com.example.devicemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.devicemanagement", "com.example.common"})
@EntityScan(basePackages = {"com.example.devicemanagement", "com.example.common"})
@EnableJpaRepositories(basePackages = "com.example.devicemanagement")
@ConfigurationPropertiesScan(basePackages = "com.example.devicemanagement")
public class DeviceManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceManagementServiceApplication.class, args);
    }
}