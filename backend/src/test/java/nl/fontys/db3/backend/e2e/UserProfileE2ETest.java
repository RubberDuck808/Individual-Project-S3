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

@DisplayName("E2E: User Profile Flow")
class UserProfileE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        setUpBase();
    }

    @Test
    @DisplayName("Register → Login → View Profile → Update Profile → Verify Changes")
    void testUserProfileUpdateFlow() throws Exception {
        // Step 1: Register
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "profileuser",
                "email", "profileuser@test.com",
                "password", "password123",
                "name", "Profile User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("profileuser"))
                .andExpect(jsonPath("$.name").value("Profile User"));

        // Step 2: Login
        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "profileuser@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String authToken = "Bearer " + objectMapper.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");

        // Step 3: View current profile
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("profileuser"))
                .andExpect(jsonPath("$.name").value("Profile User"));

        // Step 4: Update profile
        String updateRequest = objectMapper.writeValueAsString(Map.of(
                "name", "Updated Profile User"
        ));

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Profile User"));

        // Step 5: Verify changes
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Profile User"));
    }

    @Test
    @DisplayName("Register → Complete Activities → View Complete Statistics")
    void testCompleteUserStatisticsFlow() throws Exception {
        // Register and login
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "statsuser",
                "email", "statsuser@test.com",
                "password", "password123",
                "name", "Stats User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "statsuser@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String authToken = "Bearer " + objectMapper.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");

        // Create hazards
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/hazards")
                            .header("Authorization", authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "latitude", 51.4416 + (i * 0.01),
                                    "longitude", 5.4697 + (i * 0.01),
                                    "categoryId", hazardCategory.getId()
                            ))))
                    .andExpect(status().isOk());
        }

        // Complete trip
        mockMvc.perform(post("/api/trips/complete")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "startLat", 51.4416,
                                "startLng", 5.4697,
                                "endLat", 51.4500,
                                "endLng", 5.4800,
                                "distanceKm", 20.0,
                                "startedAt", java.time.OffsetDateTime.now().minusHours(1).toString(),
                                "endedAt", java.time.OffsetDateTime.now().toString()
                        ))))
                .andExpect(status().isOk());

        // View complete statistics
        mockMvc.perform(get("/api/users/{username}/stats", "statsuser")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHazardsReported").value(2))
                .andExpect(jsonPath("$.totalTrips").value(1))
                .andExpect(jsonPath("$.totalDistanceKm").value(20.0));
    }
}
