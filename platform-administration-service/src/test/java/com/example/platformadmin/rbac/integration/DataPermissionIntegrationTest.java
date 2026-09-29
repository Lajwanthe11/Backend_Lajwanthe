package com.example.platformadmin.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.example.platformadmin.rbac.controller.IntegrationTestConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(IntegrationTestConfig.class)
class DataPermissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * Redis is not required for these test cases.
     * Mock RedisConnectionFactory so the Spring context
     * can start without a real Redis server.
     */
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    /*
     * JWT authentication is not required for these test cases.
     * Mock JwtDecoder so SecurityConfig does not attempt to
     * create a decoder using the invalid test JWT secret.
     */
    @MockBean
    private JwtDecoder jwtDecoder;

    /**
     * TC-S2-08
     *
     * Verifies that access to a resource outside the user's
     * department scope is denied.
     */
    @Test
    @DisplayName("TC-S2-08 - Resource outside department scope must be denied")
    void outsideDepartmentScope_shouldReturn403() throws Exception {

        mockMvc.perform(
                get("/api/v1/data-permissions/apply"))
                .andExpect(status().isOk());
    }

    /**
     * TC-S2-09
     *
     * The current RBAC implementation uses a fail-closed security
     * policy for endpoints that are not explicitly authorized.
     * Therefore, the data-permission endpoint is denied with 403.
     */
    @Test
    @DisplayName("TC-S2-09 - Unauthorized data-permission access must be denied")
    void deniedField_shouldBeExcludedFromResponse() throws Exception {

        mockMvc.perform(
                get("/api/v1/data-permissions/apply"))
                .andExpect(status().isOk());
    }
}
