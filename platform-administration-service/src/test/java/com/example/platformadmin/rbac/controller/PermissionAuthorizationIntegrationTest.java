package com.example.platformadmin.rbac.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * End-to-end proof of the full chain: JWT auth -> @RequirePermission
 * detected -> Lavanya's (stub) resolver consulted -> allow, or generic 403
 * + logged security event.
 */
@ActiveProfiles("dev")
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

        // user-hr-1 can call POST /api/v1/users -> 200.
        @Test
        void userWithPermissionCanCreateUser() throws Exception {
                mockMvc.perform(post("/api/v1/users")
                                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwtFor("user-hr-1", "tenant-1")))
                                .contentType("application/json")
                                .content("{\"name\":\"Alice\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("created"));
        }
        // readonly user lacks USER_CREATE -> 403 and response does not reveal
        // USER_CREATE.

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
        // Read-only user can GET a user, but PUT requiring more permissions is
        // forbidden.

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
        // A user with REPORT_VIEW can call the report endpoint that uses any-of logic.

        @Test
        void requireAnyAllowsWithJustOneMatchingPermission() throws Exception {
                mockMvc.perform(get("/api/v1/users/123/report")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("user-readonly-1", "tenant-1"))))
                                .andDo(print())
                                .andExpect(status().isOk());
        }
        // No JWT -> 401 before the permission check.

        @Test
        void unauthenticatedRequestIsRejectedBeforeReachingPermissionCheck() throws Exception {
                mockMvc.perform(post("/api/v1/users").contentType("application/json").content("{}"))
                                .andExpect(status().isUnauthorized());
        }

        // Enternal service caller can validate the target user’s

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

        // Normal admin user without INTERNAL_SERVICE cannot use the internal validation
        // endpoint.

        @Test
        void internalAccessValidateRejectsNonServiceCallers() throws Exception {
                mockMvc.perform(post("/api/v1/rbac/access/validate")
                                .with(SecurityMockMvcRequestPostProcessors.jwt()
                                                .jwt(jwtFor("user-admin-1", "tenant-1")))
                                .contentType("application/json")
                                .content("{\"userId\":\"user-hr-1\",\"permissionCode\":\"USER_CREATE\"}"))
                                .andExpect(status().isForbidden());
        }

        // Admin response contains USER_DELETE, SECURITY module access, and
        // SECURITY_EVENTS menu item.

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
