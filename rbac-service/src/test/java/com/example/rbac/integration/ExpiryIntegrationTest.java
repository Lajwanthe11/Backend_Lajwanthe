package com.example.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ExpiryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * TC-S2-13
     *
     * Part 4 requires automated notification at exactly 7 days before
     * role expiry.
     *
     * Developer endpoint:
     * POST /api/v1/roles/assignments/expiry-notify
     */
    @Test
    @DisplayName("TC-S2-13 - Seven-day role expiry notification")
    void expiryNotification_shouldProcessSevenDayAssignments()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/roles/assignments/expiry-notify")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.targetExpiryDate").exists())
        .andExpect(jsonPath("$.matchedAssignments").isNumber())
        .andExpect(jsonPath("$.notificationsTriggered").isNumber());
    }
}

