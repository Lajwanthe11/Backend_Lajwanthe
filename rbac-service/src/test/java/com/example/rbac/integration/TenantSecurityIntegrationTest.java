package com.example.rbac.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TenantSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private JwtDecoder jwtDecoder;

    /*
     * TC-S2-12
     *
     * An authenticated user from Tenant A must not be able
     * to access Tenant B's role data.
     */
    @Test
    @DisplayName("TC-S2-12 - Cross tenant role access must return 403")
    void crossTenantRoleAccess_shouldReturn403() throws Exception {

        mockMvc.perform(
                get("/api/v1/roles/search")
                        .param("query", "TENANT_B_ROLE")
                        .header(
                                "X-Tenant-Id",
                                "00000000-0000-0000-0000-000000000001")
                        .with(user("tenant-a-user")
                                .roles("USER")))
                .andExpect(status().isForbidden());
    }
}
