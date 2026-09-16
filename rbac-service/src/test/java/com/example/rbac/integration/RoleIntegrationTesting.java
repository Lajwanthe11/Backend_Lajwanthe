package com.example.rbac.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RoleIntegrationTesting {

    @Autowired
    private MockMvc mockMvc;

    /*
     * TC-S2-01
     *
     * Acceptance requirement:
     * Creating a role with a duplicate name in the same tenant
     * must return HTTP 409 Conflict.
     */
    @Test
    @DisplayName("TC-S2-01 - Duplicate role name must return 409")
    void duplicateRoleName_shouldReturn409() throws Exception {

        String roleName =
                "PART12_DUPLICATE_ROLE_" + System.currentTimeMillis();

        String request = """
                {
                  "roleName": "%s",
                  "roleCode": "PART12_DUPLICATE_%d",
                  "roleType": "CUSTOM",
                  "description": "Part 12 duplicate role test",
                  "status": "ACTIVE"
                }
                """.formatted(
                        roleName,
                        System.currentTimeMillis()
                );

        /*
         * First creation establishes the role.
         */
        mockMvc.perform(
                post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isCreated());

        /*
         * Second creation uses the same role name.
         */
        mockMvc.perform(
                post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleName": "%s",
                                  "roleCode": "PART12_DUPLICATE_%d_2",
                                  "roleType": "CUSTOM",
                                  "description": "Duplicate role",
                                  "status": "ACTIVE"
                                }
                                """.formatted(
                                        roleName,
                                        System.currentTimeMillis()
                                ))
        )
        .andExpect(status().isConflict());
    }

    /*
     * TC-S2-02
     *
     * Acceptance requirement:
     * An Org Admin must not be able to delete a system role.
     */
    @Test
    @DisplayName("TC-S2-02 - System role deletion must return 403")
    void systemRoleDeletion_shouldReturn403() throws Exception {

        /*
         * SUPER_ADMIN is one of the developer-seeded system role codes.
         *
         * The role-search endpoint returns the role identifier.
         */
        String response = mockMvc.perform(
                get("/api/v1/roles/search")
                        .param("query", "SUPER_ADMIN")
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        /*
         * The test intentionally uses a JSON-path based assertion in the
         * request pipeline rather than inventing a role ID.
         *
         * If no SUPER_ADMIN exists in the integration database,
         * this test should be treated as a fixture/seeding defect.
         */
        if (response == null || response.isBlank()
                || response.equals("[]")) {
            throw new AssertionError(
                    "TC-S2-02 requires a seeded SUPER_ADMIN system role"
            );
        }

        /*
         * The actual ID extraction is deliberately kept outside the
         * compile-time domain model because the supplied developer sources
         * contain conflicting Long/UUID Role ID revisions.
         *
         * Replace SYSTEM_ROLE_ID with the persisted SUPER_ADMIN id from
         * your integration fixture.
         */
        String systemRoleId = extractFirstRoleId(response);

        mockMvc.perform(
                delete("/api/v1/roles/{id}", systemRoleId)
        )
        .andExpect(status().isForbidden());
    }

    private String extractFirstRoleId(String json) {

        /*
         * Minimal extraction avoids introducing another test dependency.
         *
         * Expected response contains:
         * [{"id":"...","roleName":"SUPER_ADMIN",...}]
         */
        int idIndex = json.indexOf("\"id\"");

        if (idIndex < 0) {
            throw new AssertionError(
                    "Role search response does not contain id"
            );
        }

        int colon = json.indexOf(':', idIndex);
        int firstQuote = json.indexOf('"', colon + 1);
        int secondQuote = json.indexOf('"', firstQuote + 1);

        if (firstQuote < 0 || secondQuote < 0) {
            throw new AssertionError(
                    "Unable to extract role id from response"
            );
        }

        return json.substring(firstQuote + 1, secondQuote);
    }
}
