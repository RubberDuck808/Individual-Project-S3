package nl.fontys.db3.backend.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("E2E: Hazard Reporting Flow")
class HazardReportingE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        setUpBase();
    }

    @Test
    @DisplayName("User Registration → Login → Create Hazard → Vote → View Statistics")
    void testCompleteHazardReportingFlow() throws Exception {
        // Step 1: Register a new user
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "e2euser",
                "email", "e2euser@test.com",
                "password", "password123",
                "name", "E2E Test User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("e2euser"))
                .andExpect(jsonPath("$.email").value("e2euser@test.com"));

        // Step 2: Login to get authentication token
        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "e2euser@test.com",
                "password", "password123"
        ));

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("e2euser"))
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        Map<String, Object> loginData = objectMapper.readValue(loginResponse, new TypeReference<Map<String, Object>>() {});
        String authToken = "Bearer " + loginData.get("token");

        // Step 3: Create a hazard report
        String hazardRequest = objectMapper.writeValueAsString(Map.of(
                "latitude", 51.4416,
                "longitude", 5.4697,
                "categoryId", hazardCategory.getId()
        ));

        MvcResult hazardResult = mockMvc.perform(post("/api/hazards")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(hazardRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(51.4416))
                .andExpect(jsonPath("$.longitude").value(5.4697))
                .andExpect(jsonPath("$.category").value(hazardCategory.getName()))
                .andReturn();

        String hazardResponse = hazardResult.getResponse().getContentAsString();
        Map<String, Object> hazardData = objectMapper.readValue(hazardResponse, new TypeReference<Map<String, Object>>() {});
        Long hazardId = Long.valueOf(hazardData.get("id").toString());

        // Step 4: Register a second user to vote on the hazard
        String user2RegisterRequest = objectMapper.writeValueAsString(Map.of(
                "username", "e2evoter",
                "email", "e2evoter@test.com",
                "password", "password123",
                "name", "E2E Voter"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2RegisterRequest))
                .andExpect(status().isOk());

        String user2LoginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "e2evoter@test.com",
                "password", "password123"
        ));

        MvcResult user2LoginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2LoginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String user2LoginResponse = user2LoginResult.getResponse().getContentAsString();
        Map<String, Object> user2LoginData = objectMapper.readValue(user2LoginResponse, new TypeReference<Map<String, Object>>() {});
        String user2Token = "Bearer " + user2LoginData.get("token");

        // Step 5: Vote on the hazard
        String voteRequest = objectMapper.writeValueAsString(Map.of(
                "hazardId", hazardId,
                "voteType", "UPVOTE"
        ));

        mockMvc.perform(post("/api/votes")
                        .header("Authorization", user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(voteRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteType").value("UPVOTE"))
                .andExpect(jsonPath("$.hazardId").value(hazardId));

        // Step 6: Check vote counts
        mockMvc.perform(get("/api/votes/{hazardId}/count", hazardId)
                        .header("Authorization", user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(1))
                .andExpect(jsonPath("$.downvotes").value(0));

        // Step 7: View user statistics
        mockMvc.perform(get("/api/users/{username}/stats", "e2euser")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHazardsReported").value(1))
                .andExpect(jsonPath("$.totalVotes").value(0)); // User hasn't voted on their own hazard

        mockMvc.perform(get("/api/users/{username}/stats", "e2evoter")
                        .header("Authorization", user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1));
    }

    @Test
    @DisplayName("Create Multiple Hazards → View User Hazards → View Statistics")
    void testUserHazardHistoryFlow() throws Exception {
        // Step 1: Register and login
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "hazardhistory",
                "email", "hazardhistory@test.com",
                "password", "password123",
                "name", "Hazard History User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "hazardhistory@test.com",
                "password", "password123"
        ));

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        Map<String, Object> loginData = objectMapper.readValue(loginResponse, new TypeReference<Map<String, Object>>() {});
        String authToken = "Bearer " + loginData.get("token");

        // Step 2: Create multiple hazards
        for (int i = 0; i < 3; i++) {
            String hazardRequest = objectMapper.writeValueAsString(Map.of(
                    "latitude", 51.4416 + (i * 0.01),
                    "longitude", 5.4697 + (i * 0.01),
                    "categoryId", hazardCategory.getId()
            ));

            mockMvc.perform(post("/api/hazards")
                            .header("Authorization", authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(hazardRequest))
                    .andExpect(status().isOk());
        }

        // Step 3: View all open hazards
        mockMvc.perform(get("/api/hazards/open")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));

        // Step 4: View hazards by this specific user
        mockMvc.perform(get("/api/hazards/by-user/{username}", "hazardhistory")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        // Step 5: Verify statistics reflect the hazards
        mockMvc.perform(get("/api/users/{username}/stats", "hazardhistory")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHazardsReported").value(3));
    }
}
