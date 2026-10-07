package com.example.platformadmin.rbac.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.web.servlet.MockMvc;

import com.example.platformadmin.rbac.controller.IntegrationTestConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestConfig.class)
class JwtSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        doThrow(new JwtException("Invalid JWT token"))
                .when(jwtDecoder)
                .decode(anyString());
    }

    @Test
    void missingJwt_shouldReturn401() throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwt_shouldBeRejected() {

        assertThrows(
                Exception.class,
                () -> mockMvc.perform(
                        get("/api/v1/users/user-hr-1")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"))
        );
    }

    @Test
    void malformedJwt_shouldBeRejected() {

        assertThrows(
                Exception.class,
                () -> mockMvc.perform(
                        get("/api/v1/users/user-hr-1")
                                .header(
                                        "Authorization",
                                        "Bearer abc.def"))
        );
    }

    @Test
    void tamperedJwt_shouldBeRejected() {

        String tamperedToken =
                "eyJhbGciOiJIUzI1NiJ9."
              + "tampered-payload."
              + "invalid-signature";

        assertThrows(
                Exception.class,
                () -> mockMvc.perform(
                        get("/api/v1/users/user-hr-1")
                                .header(
                                        "Authorization",
                                        "Bearer " + tamperedToken))
        );
    }

    @Test
    void bearerTokenMissing_shouldReturn401() throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1"))
                .andExpect(status().isUnauthorized());
    }
}
