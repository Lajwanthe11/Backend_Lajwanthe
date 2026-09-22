package com.example.rbac.integration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.rbac.config.SecurityContextUtil;
import com.example.rbac.dto.AuthenticatedUser;
import com.example.rbac.service.PermissionAuthorizationService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ExpiryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * Redis is not required for this test.
     * Mock RedisConnectionFactory so the Spring context
     * can start without a real Redis server.
     */
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    /*
     * JWT authentication is not required for this test.
     * Mock JwtDecoder so SecurityConfig does not attempt
     * to decode the invalid test JWT secret.
     */
    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private PermissionAuthorizationService permissionAuthorizationService;

    @MockBean
    private SecurityContextUtil securityContextUtil;

    @BeforeEach
    void setUp() {
        when(securityContextUtil.currentUser())
                .thenReturn(new AuthenticatedUser(
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString()
                ));
    }

    @Test
    @DisplayName("TC-S2-13 - Seven-day role expiry notification")
    void expiryNotification_shouldProcessSevenDayAssignments()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/roles/assignments/expiry-notify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetExpiryDate").exists())
                .andExpect(jsonPath("$.matchedAssignments").isNumber())
                .andExpect(jsonPath("$.notificationsTriggered").isNumber());
    }
}
