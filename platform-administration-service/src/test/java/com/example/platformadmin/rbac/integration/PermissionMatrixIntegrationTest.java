package com.example.platformadmin.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PermissionMatrixIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * Redis is not required for these tests.
     * Mock RedisConnectionFactory so the Spring context
     * can start without a real Redis server.
     */
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    /*
     * JWT authentication is not required for these tests.
     * Mock JwtDecoder so SecurityConfig does not attempt
     * to create a decoder using the invalid test JWT secret.
     */
    @MockBean
    private JwtDecoder jwtDecoder;

    /*
     * TC-S2-07
     *
     * One invalid permission in a batch update must cause the transaction
     * to roll back.
     *
     * Developer contract:
     * roleId          = Long
     * permissionId    = UUID
     * expectedVersion = Long
     */
    @Test
    @DisplayName("TC-S2-07 - Invalid permission in batch must rollback")
    void invalidPermissionInBatch_shouldRollback() throws Exception {

        long roleId = 1L;

        UUID invalidPermissionId = UUID.randomUUID();

        String request = """
                {
                  "permissions": [
                    {
                      "permissionId": "%s",
                      "granted": true
                    }
                  ],
                  "expectedVersion": 0
                }
                """.formatted(invalidPermissionId);

        mockMvc.perform(
                put("/api/v1/roles/{roleId}/permissions/batch", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(result -> {
                    int responseStatus = result.getResponse().getStatus();

                    if (responseStatus < 400 || responseStatus >= 500) {
                        throw new AssertionError(
                                "Invalid permission batch must not succeed. "
                                        + "Received HTTP " + responseStatus);
                    }
                });
    }

    @Test
    @DisplayName("Batch permission update must reject empty permissions")
    void emptyPermissions_shouldReturn400() throws Exception {

        mockMvc.perform(
                put("/api/v1/roles/{roleId}/permissions/batch", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissions": [],
                                  "expectedVersion": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Batch permission update must reject missing expectedVersion")
    void missingExpectedVersion_shouldReturn400() throws Exception {

        mockMvc.perform(
                put("/api/v1/roles/{roleId}/permissions/batch", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissions": [
                                    {
                                      "permissionId": "%s",
                                      "granted": true
                                    }
                                  ]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Batch permission update must reject null permissionId")
    void nullPermissionId_shouldReturn400() throws Exception {

        mockMvc.perform(
                put("/api/v1/roles/{roleId}/permissions/batch", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissions": [
                                    {
                                      "permissionId": null,
                                      "granted": true
                                    }
                                  ],
                                  "expectedVersion": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Batch permission update must reject null granted")
    void nullGranted_shouldReturn400() throws Exception {

        mockMvc.perform(
                put("/api/v1/roles/{roleId}/permissions/batch", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissions": [
                                    {
                                      "permissionId": "%s",
                                      "granted": null
                                    }
                                  ],
                                  "expectedVersion": 0
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }
}
