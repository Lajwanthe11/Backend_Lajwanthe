package com.example.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BulkAssignmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * Redis is not required for TC-S2-04.
     *
     * RedisConfig requires a RedisConnectionFactory when creating
     * the application's RedisTemplate. We provide a mock factory
     * so the Spring context can start without a real Redis server.
     */
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    @DisplayName("TC-S2-04 - Bulk assignment of 600 users must return 400")
    void bulkAssign600Users_shouldReturn400() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        String userIds = IntStream.range(0, 600)
                .mapToObj(i -> "\"" + UUID.randomUUID() + "\"")
                .collect(Collectors.joining(","));

        String request = """
                {
                  "userIds": [%s],
                  "roleId": "%s",
                  "effectiveDate": "2099-01-01",
                  "expiryDate": "2099-12-31",
                  "reason": "Part 12 bulk assignment test"
                }
                """.formatted(userIds, roleId);

        mockMvc.perform(
                post("/api/v1/users/roles/bulk-assign")
                        .header("X-Tenant-Id", tenantId.toString())
                        .header("X-User-Id", actorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest());
    }
}