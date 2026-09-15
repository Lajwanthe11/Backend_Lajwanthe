package com.example.rbac.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RbacEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedUserEndpoint_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void securityEventsEndpoint_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/security/events")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void permissionSearch_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/permissions/search")
                        .param("query", "USER")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void accessValidationEndpoint_withoutAuthentication_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/rbac/access/validate")
                        .contentType("application/json")
                        .content("""
                                {
                                  "userId": "user-hr-1",
                                  "permissionCode": "USER_READ",
                                  "resourceType": "USER",
                                  "resourceId": "100"
                                }
                                """)
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwt_shouldNotReachRbacLayer()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void crossTenantRequest_shouldBeRejected()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1")
                        .header(
                                "Authorization",
                                "Bearer tenant-b-invalid-token"
                        )
        )
        .andExpect(status().isUnauthorized());
    }
}