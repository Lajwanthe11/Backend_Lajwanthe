package com.example.platformadmin.rbac.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.platformadmin.rbac.controller.UserRoleController;
import com.example.platformadmin.rbac.dto.request.AssignRoleRequest;
import com.example.platformadmin.rbac.exception.InvalidRoleAssignmentException;
import com.example.platformadmin.rbac.service.UserRoleService;

@WebMvcTest(UserRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRoleService userRoleService;

    @Test
    @DisplayName("TC-S2-03 - Invalid expiry date must be rejected")
    void invalidExpiryDate_shouldBeRejected() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(userRoleService.assignRoles(
                eq(userId),
                any(AssignRoleRequest.class)))
                .thenThrow(new InvalidRoleAssignmentException(
                        "Expiry date must be after effective date"));

        mockMvc.perform(
                post("/api/v1/users/{userId}/roles", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roles": [
                                    {
                                      "roleId": "%s",
                                      "primary": false,
                                      "effectiveDate": "2026-09-20",
                                      "expiryDate": "2026-09-20"
                                    }
                                  ]
                                }
                                """.formatted(roleId)))
                .andExpect(status().isInternalServerError());
    }
}
