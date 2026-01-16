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

@DisplayName("E2E: Voting Flow")
class VotingE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        setUpBase();
    }

    @Test
    @DisplayName("Create Hazard → Multiple Users Vote → Check Vote Counts → View My Vote")
    void testMultipleVotesFlow() throws Exception {
        // Register and login user 1 (hazard creator)
        String user1Register = objectMapper.writeValueAsString(Map.of(
                "username", "votecreator",
                "email", "votecreator@test.com",
                "password", "password123",
                "name", "Vote Creator"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Register))
                .andExpect(status().isOk());

        MvcResult user1Login = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "votecreator@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String user1Token = "Bearer " + objectMapper.readValue(user1Login.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");

        // Create hazard
        MvcResult hazardResult = mockMvc.perform(post("/api/hazards")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "latitude", 51.4416,
                                "longitude", 5.4697,
                                "categoryId", hazardCategory.getId()
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        Long hazardId = Long.valueOf(objectMapper.readValue(hazardResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("id").toString());

        // Register and login multiple voters
        String[] voters = {"voter1", "voter2", "voter3"};
        String[] tokens = new String[3];

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "username", voters[i],
                                    "email", voters[i] + "@test.com",
                                    "password", "password123",
                                    "name", "Voter " + (i + 1)
                            ))))
                    .andExpect(status().isOk());

            MvcResult login = mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "email", voters[i] + "@test.com",
                                    "password", "password123"
                            ))))
                    .andExpect(status().isOk())
                    .andReturn();

            tokens[i] = "Bearer " + objectMapper.readValue(login.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");
        }

        // Voter 1 and 2 upvote, Voter 3 downvotes
        mockMvc.perform(post("/api/votes")
                        .header("Authorization", tokens[0])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "hazardId", hazardId,
                                "voteType", "UPVOTE"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/votes")
                        .header("Authorization", tokens[1])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "hazardId", hazardId,
                                "voteType", "UPVOTE"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/votes")
                        .header("Authorization", tokens[2])
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "hazardId", hazardId,
                                "voteType", "DOWNVOTE"
                        ))))
                .andExpect(status().isOk());

        // Check vote counts
        mockMvc.perform(get("/api/votes/{hazardId}/count", hazardId)
                        .header("Authorization", tokens[0]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(2))
                .andExpect(jsonPath("$.downvotes").value(1));

        // Check individual votes
        mockMvc.perform(get("/api/votes/{hazardId}/mine", hazardId)
                        .header("Authorization", tokens[0]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteType").value("UPVOTE"));

        mockMvc.perform(get("/api/votes/{hazardId}/mine", hazardId)
                        .header("Authorization", tokens[2]))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteType").value("DOWNVOTE"));
    }
}
