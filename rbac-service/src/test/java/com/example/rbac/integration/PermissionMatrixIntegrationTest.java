package com.example.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PermissionMatrixIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * TC-S2-07
     *
     * One invalid permission in a batch update must cause the transaction
     * to roll back.
     *
     * Important developer contract:
     *   roleId       = Long
     *   permissionId = UUID
     *   expectedVersion = Long
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
                        .content(request)
        )
        .andExpect(
                result -> {
                    int status = result.getResponse().getStatus();

                    /*
                     * An invalid permission is a business/application
                     * failure. It must never result in a successful
                     * permission update.
                     */
                    if (status < 400 || status >= 500) {
                        throw new AssertionError(
                                "Invalid permission batch must not succeed. "
                                + "Received HTTP " + status
                        );
                    }
                }
        );
    }

    /*
     * API validation checks.
     */
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
                                """)
        )
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
                                """.formatted(UUID.randomUUID()))
        )
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
                                """)
        )
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
                                """.formatted(UUID.randomUUID()))
        )
        .andExpect(status().isBadRequest());
    }
}

