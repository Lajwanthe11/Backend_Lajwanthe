package com.example.platformadmin.rbac.controller;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DevTokenControllerTest {

        private static final String TEST_SECRET = "test-secret-key-at-least-32-chars-long!!";

        private MockMvc mockMvc;
        private DevTokenController controller;

        @BeforeEach
        void setUp() {
                controller = new DevTokenController();
                ReflectionTestUtils.setField(controller, "jwtSecret", TEST_SECRET);
                mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

        @Test
        void issueDemoToken_withDefaultParams_shouldReturn200() throws Exception {
                mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk());
        }

        // Response body must contain the "token" key when defaults are used.

        @Test
        void issueDemoToken_withDefaultParams_shouldReturnTokenKey() throws Exception {
                mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.token").isNotEmpty());
        }

        // Response body must contain the "usage" key with the correct hint text.

        @Test
        void issueDemoToken_withDefaultParams_shouldReturnUsageKey() throws Exception {
                mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.usage").value("Authorization: Bearer <token>"));
        }

        @Test
        void issueDemoToken_withCustomParams_shouldEmbedClaimsInToken() throws Exception {
                MvcResult result = mockMvc.perform(
                                get("/api/v1/dev/token")
                                                .param("userId", "user-custom-99")
                                                .param("tenantId", "tenant-42"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andReturn();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());
                SignedJWT signedJWT = SignedJWT.parse(token);

                assertThat(signedJWT.getJWTClaimsSet().getSubject()).isEqualTo("user-custom-99");
                assertThat(signedJWT.getJWTClaimsSet().getStringClaim("userId")).isEqualTo("user-custom-99");
                assertThat(signedJWT.getJWTClaimsSet().getStringClaim("tenantId")).isEqualTo("tenant-42");
        }

        // Only userId supplied; tenantId should fall back to the default "tenant-1".

        @Test
        void issueDemoToken_withOnlyUserId_shouldUseDefaultTenantId() throws Exception {
                MvcResult result = mockMvc.perform(
                                get("/api/v1/dev/token")
                                                .param("userId", "user-abc"))
                                .andExpect(status().isOk())
                                .andReturn();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());
                SignedJWT jwt = SignedJWT.parse(token);

                assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("user-abc");
                assertThat(jwt.getJWTClaimsSet().getStringClaim("tenantId")).isEqualTo("tenant-1");
        }

        // Only tenantId supplied; userId should fall back to the default "user-hr-1".

        @Test
        void issueDemoToken_withOnlyTenantId_shouldUseDefaultUserId() throws Exception {
                MvcResult result = mockMvc.perform(
                                get("/api/v1/dev/token")
                                                .param("tenantId", "tenant-xyz"))
                                .andExpect(status().isOk())
                                .andReturn();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());
                SignedJWT jwt = SignedJWT.parse(token);

                assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("user-hr-1");
                assertThat(jwt.getJWTClaimsSet().getStringClaim("tenantId")).isEqualTo("tenant-xyz");
        }

        // The returned token must be a valid, parseable, signed JWT.

        @Test
        void issueDemoToken_shouldReturnValidSignedJwt() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk())
                                .andReturn();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());

                // Must parse without throwing
                SignedJWT jwt = SignedJWT.parse(token);
                assertThat(jwt).isNotNull();
                // A JWT must have exactly three dot-separated parts
                assertThat(token.split("\\.")).hasSize(3);
        }

        // The JWT must include a non-null issue-time claim.

        @Test
        void issueDemoToken_shouldIncludeIssueTimeClaim() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk())
                                .andReturn();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());
                SignedJWT jwt = SignedJWT.parse(token);

                assertThat(jwt.getJWTClaimsSet().getIssueTime()).isNotNull();
        }

        @Test
        void issueDemoToken_shouldSetExpirationToApproximatelyOneHour() throws Exception {
                long beforeCall = System.currentTimeMillis();

                MvcResult result = mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk())
                                .andReturn();

                long afterCall = System.currentTimeMillis();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());
                SignedJWT jwt = SignedJWT.parse(token);

                long expiryMs = jwt.getJWTClaimsSet().getExpirationTime().getTime();
                long oneHour = 3_600_000L;

                // Expiry must be within [beforeCall + 1h, afterCall + 1h] (with 2000 ms margin for second truncation)

                assertThat(expiryMs).isGreaterThanOrEqualTo(beforeCall + oneHour - 2000);
                assertThat(expiryMs).isLessThanOrEqualTo(afterCall + oneHour + 2000);
        }

        // The JWT subject claim must equal the userId query param.

        @Test
        void issueDemoToken_subjectClaimShouldMatchUserId() throws Exception {
                MvcResult result = mockMvc.perform(
                                get("/api/v1/dev/token")
                                                .param("userId", "subject-test-user"))
                                .andExpect(status().isOk())
                                .andReturn();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());
                SignedJWT jwt = SignedJWT.parse(token);

                assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("subject-test-user");
        }

        // The response map must contain both "token" and "usage" keys.

        @Test
        void issueDemoToken_responseShouldContainBothKeys() throws Exception {
                mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.usage").exists());
        }

        @Test
        void issueDemoToken_usageValueShouldBeStaticHintString() throws Exception {
                mockMvc.perform(get("/api/v1/dev/token")
                                .param("userId", "any-user")
                                .param("tenantId", "any-tenant"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.usage").value("Authorization: Bearer <token>"));
        }

        // When no params are provided, the JWT subject should default to "user-hr-1".

        @Test
        void issueDemoToken_defaultSubjectShouldBeUserHr1() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk())
                                .andReturn();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());
                SignedJWT jwt = SignedJWT.parse(token);

                assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("user-hr-1");
        }

        // When no params are provided, the tenantId claim should default to "tenant-1".

        @Test
        void issueDemoToken_defaultTenantIdShouldBeTenant1() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/v1/dev/token"))
                                .andExpect(status().isOk())
                                .andReturn();

                String token = extractTokenFromJson(result.getResponse().getContentAsString());
                SignedJWT jwt = SignedJWT.parse(token);

                assertThat(jwt.getJWTClaimsSet().getStringClaim("tenantId")).isEqualTo("tenant-1");
        }

        private String extractTokenFromJson(String json) {
                int start = json.indexOf("\"token\":\"") + "\"token\":\"".length();
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
        }
}
