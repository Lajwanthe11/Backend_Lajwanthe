package com.example.rbac.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.rbac.controller.UserRoleController;
import com.example.rbac.dto.request.AssignRoleRequest;
import com.example.rbac.service.UserRoleService;

@WebMvcTest(UserRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomRoleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRoleService userRoleService;

    @Test
    @DisplayName("TC-S2-10 - Draft custom role assignment endpoint should be accessible")
    void draftCustomRole_shouldNotBeAssignable() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(userRoleService.assignRoles(
                any(UUID.class),
                any(AssignRoleRequest.class)))
                .thenReturn(Collections.emptyList());

        String request = """
                {
                  "roles": [
                    {
                      "roleId": "%s",
                      "primary": false,
                      "effectiveDate": "2026-09-21"
                    }
                  ]
                }
                """.formatted(roleId);

        mockMvc.perform(
                post("/api/v1/users/{userId}/roles", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-S2-11 - Custom role assignment endpoint should return 200")
    void publishCustomRole_shouldReturn200() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        when(userRoleService.assignRoles(
                any(UUID.class),
                any(AssignRoleRequest.class)))
                .thenReturn(Collections.emptyList());

        String request = """
                {
                  "roles": [
                    {
                      "roleId": "%s",
                      "primary": false,
                      "effectiveDate": "2026-09-21"
                    }
                  ]
                }
                """.formatted(roleId);

        mockMvc.perform(
                post("/api/v1/users/{userId}/roles", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }
}
