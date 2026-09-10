package com.enterprise.superadmin.platform_health_service.service;

import com.enterprise.superadmin.platform_health_service.dto.response.DatabaseHealthResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHealthService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DatabaseHealthResponse getDatabaseHealth() {

        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            return DatabaseHealthResponse.builder()
                    .status("HEALTHY")
                    .availability(true)
                    .build();

        } catch (Exception exception) {

            return DatabaseHealthResponse.builder()
                    .status("FAILED")
                    .availability(false)
                    .build();
        }
    }
}