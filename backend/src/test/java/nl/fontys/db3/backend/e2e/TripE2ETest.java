package nl.fontys.db3.backend.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("E2E: Trip Flow")
class TripE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        setUpBase();
    }

    @Test
    @DisplayName("User Registration → Login → Complete Trip → View Statistics")
    void testCompleteTripFlow() throws Exception {
        // Step 1: Register and login
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "triptest",
                "email", "triptest@test.com",
                "password", "password123",
                "name", "Trip Test User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "triptest@test.com",
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

        // Step 2: Complete a trip
        OffsetDateTime startTime = OffsetDateTime.now().minusHours(1);
        OffsetDateTime endTime = OffsetDateTime.now();

        Map<String, Object> tripRequest = Map.of(
                "startLat", 51.4416,
                "startLng", 5.4697,
                "endLat", 51.4500,
                "endLng", 5.4800,
                "distanceKm", 15.5,
                "startedAt", startTime.toString(),
                "endedAt", endTime.toString()
        );

        String tripRequestJson = objectMapper.writeValueAsString(tripRequest);

        mockMvc.perform(post("/api/trips/complete")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tripRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(15.5))
                .andExpect(jsonPath("$.startLat").value(51.4416))
                .andExpect(jsonPath("$.endLat").value(51.4500));

        // Step 3: View user statistics to verify trip was recorded
        mockMvc.perform(get("/api/users/{username}/stats", "triptest")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(1))
                .andExpect(jsonPath("$.totalDistanceKm").value(15.5));
    }

    @Test
    @DisplayName("Complete Multiple Trips → Verify Statistics Aggregation")
    void testMultipleTripsStatisticsFlow() throws Exception {
        // Register and login
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "multitrip",
                "email", "multitrip@test.com",
                "password", "password123",
                "name", "Multi Trip User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "multitrip@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String authToken = "Bearer " + objectMapper.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");

        // Complete multiple trips
        double totalDistance = 0.0;
        for (int i = 0; i < 3; i++) {
            double distance = 10.0 + (i * 5.0);
            totalDistance += distance;

            Map<String, Object> tripRequest = Map.of(
                    "startLat", 51.4416 + (i * 0.01),
                    "startLng", 5.4697 + (i * 0.01),
                    "endLat", 51.4500 + (i * 0.01),
                    "endLng", 5.4800 + (i * 0.01),
                    "distanceKm", distance,
                    "startedAt", OffsetDateTime.now().minusHours(3 - i).toString(),
                    "endedAt", OffsetDateTime.now().minusHours(2 - i).toString()
            );

            mockMvc.perform(post("/api/trips/complete")
                            .header("Authorization", authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tripRequest)))
                    .andExpect(status().isOk());
        }

        // Verify aggregated statistics
        mockMvc.perform(get("/api/users/{username}/stats", "multitrip")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrips").value(3))
                .andExpect(jsonPath("$.totalDistanceKm").value(totalDistance));
    }
}
