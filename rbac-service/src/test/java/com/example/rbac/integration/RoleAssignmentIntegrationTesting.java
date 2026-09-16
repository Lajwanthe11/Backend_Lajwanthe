package com.example.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAssignmentIntegrationTesting {

    @Autowired
    private MockMvc mockMvc;

    /*
     * TC-S2-03
     *
     * A newly assigned role cannot have an effective date in the past.
     */
    @Test
    @DisplayName("TC-S2-03 - Past effective date must return 400")
    void pastEffectiveDate_shouldReturn400() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        String yesterday =
                LocalDate.now().minusDays(1).toString();

        mockMvc.perform(
                post("/api/v1/users/{userId}/roles", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roles": [
                                    {
                                      "roleId": "%s",
                                      "primary": false,
                                      "effectiveDate": "%s"
                                    }
                                  ]
                                }
                                """.formatted(roleId, yesterday))
        )
        .andExpect(status().isBadRequest());
    }
}

