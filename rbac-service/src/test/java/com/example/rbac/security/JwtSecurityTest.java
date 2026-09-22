package com.example.rbac.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.rbac.controller.IntegrationTestConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestConfig.class)
class JwtSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void missingJwt_shouldReturn401() throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwt_shouldReturn401() throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1")
                        .header("Authorization", "Bearer invalid-token")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void malformedJwt_shouldReturn401() throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1")
                        .header(
                                "Authorization",
                                "Bearer abc.def"
                        )
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedJwt_shouldReturn401() throws Exception {

        String tamperedToken =
                "eyJhbGciOiJIUzI1NiJ9."
              + "tampered-payload."
              + "invalid-signature";

        mockMvc.perform(
                get("/api/v1/users/user-hr-1")
                        .header(
                                "Authorization",
                                "Bearer " + tamperedToken
                        )
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void bearerTokenMissing_shouldReturn401() throws Exception {

        mockMvc.perform(
                get("/api/v1/users/user-hr-1")
        )
        .andExpect(status().isUnauthorized());
    }
}
