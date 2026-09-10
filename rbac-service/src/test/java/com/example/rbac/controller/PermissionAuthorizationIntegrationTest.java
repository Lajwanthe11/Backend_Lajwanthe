package com.example.rbac.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end proof of the full chain: JWT auth -> @RequirePermission
 * detected -> Lavanya's (stub) resolver consulted -> allow, or generic 403
 * + logged security event.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestConfig.class)
class PermissionAuthorizationIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        private Jwt jwtFor(String userId, String tenantId) {
                return Jwt.withTokenValue("test-token")
                                .header("alg", "none")
                                .claim("userId", userId)
                                .claim("tenantId", tenantId)
                                .subject(userId)
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(3600))
                                .build();
        }

        @Test
        void userWithPermissionCanCreateUser() throws Exception {
                mockMvc.perform(post("/api/v1/users")
                                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwtFor("user-hr-1", "tenant-1")))
                                .contentType("application/json")
                                .content("{\"name\":\"Alice\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("created"));
        }

        @Test
        void userWithoutPermissionGetsGeneric403AndNoLeakedPermissionDetail() throws Exception {
                mockMvc.perform(post("/api/v1/users")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("user-readonly-1", "tenant-1")))
                                .contentType("application/json")
                                .content("{\"name\":\"Bob\"}"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value(403))
                                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                                .andExpect(jsonPath("$.message")
                                                .value("You do not have permission to perform this action"))
                                .andExpect(content().string(org.hamcrest.Matchers.not(
                                                org.hamcrest.Matchers.containsString("USER_CREATE"))));
        }

        @Test
        void requireAllRejectsWhenOnlyOnePermissionPresent() throws Exception {
                mockMvc.perform(get("/api/v1/users/123")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("user-readonly-1", "tenant-1"))))
                                .andExpect(status().isOk());

                mockMvc.perform(put("/api/v1/users/123")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("user-readonly-1", "tenant-1")))
                                .contentType("application/json")
                                .content("{}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void requireAnyAllowsWithJustOneMatchingPermission() throws Exception {
                mockMvc.perform(get("/api/v1/users/123/report")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("user-readonly-1", "tenant-1"))))
                                .andExpect(status().isOk());
        }

        @Test
        void unauthenticatedRequestIsRejectedBeforeReachingPermissionCheck() throws Exception {
                mockMvc.perform(post("/api/v1/users").contentType("application/json").content("{}"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void internalAccessValidateChecksTargetUserNotCaller() throws Exception {
                mockMvc.perform(post("/api/v1/rbac/access/validate")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("svc-internal-1", "tenant-1")))
                                .contentType("application/json")
                                .content("{\"userId\":\"user-hr-1\",\"permissionCode\":\"USER_CREATE\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.userId").value("user-hr-1"))
                                .andExpect(jsonPath("$.allowed").value(true));
        }

        @Test
        void internalAccessValidateRejectsNonServiceCallers() throws Exception {
                mockMvc.perform(post("/api/v1/rbac/access/validate")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("user-admin-1", "tenant-1")))
                                .contentType("application/json")
                                .content("{\"userId\":\"user-hr-1\",\"permissionCode\":\"USER_CREATE\"}"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void uiPermissionApiReflectsCurrentUserPermissions() throws Exception {
                mockMvc.perform(get("/api/v1/auth/me/permissions")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("user-admin-1", "tenant-1"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.permissions", org.hamcrest.Matchers.hasItem("USER_DELETE")))
                                .andExpect(jsonPath("$.modules.SECURITY.canAccess").value(true))
                                .andExpect(jsonPath("$.menuItems", org.hamcrest.Matchers.hasItem("SECURITY_EVENTS")));
        }
}
