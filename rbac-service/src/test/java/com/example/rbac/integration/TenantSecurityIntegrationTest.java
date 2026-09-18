package com.example.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TenantSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * TC-S2-12
     *
     * Tenant A must not be able to access tenant B's role list.
     *
     * The actual tenant is expected to come from the authenticated security
     * context in the production implementation.
     */
    @Test
    @DisplayName("TC-S2-12 - Cross tenant role access must return 403")
    void crossTenantRoleAccess_shouldReturn403() throws Exception {

        mockMvc.perform(
                get("/api/v1/roles/search")
                        .param("query", "TENANT_B_ROLE")
                        .header("X-Tenant-Id",
                                "00000000-0000-0000-0000-000000000001")
        )
        .andExpect(status().isForbidden());
    }
}

