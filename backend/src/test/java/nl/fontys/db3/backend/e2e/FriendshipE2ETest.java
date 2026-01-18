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

@DisplayName("E2E: Friendship Flow")
class FriendshipE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        setUpBase();
    }

    @Test
    @DisplayName("Two Users → Register → Login → Send Friend Request → Accept → View Friends")
    void testCompleteFriendshipFlow() throws Exception {
        // Step 1: Register two users
        String user1Register = objectMapper.writeValueAsString(Map.of(
                "username", "frienduser1",
                "email", "frienduser1@test.com",
                "password", "password123",
                "name", "Friend User 1"
        ));

        String user2Register = objectMapper.writeValueAsString(Map.of(
                "username", "frienduser2",
                "email", "frienduser2@test.com",
                "password", "password123",
                "name", "Friend User 2"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Register))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Register))
                .andExpect(status().isOk());

        // Step 2: Login both users
        String user1Login = objectMapper.writeValueAsString(Map.of(
                "email", "frienduser1@test.com",
                "password", "password123"
        ));

        String user2Login = objectMapper.writeValueAsString(Map.of(
                "email", "frienduser2@test.com",
                "password", "password123"
        ));

        MvcResult user1LoginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Login))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult user2LoginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Login))
                .andExpect(status().isOk())
                .andReturn();

        String user1LoginResponse = user1LoginResult.getResponse().getContentAsString();
        Map<String, Object> user1LoginData = objectMapper.readValue(user1LoginResponse, new TypeReference<Map<String, Object>>() {});
        String user1Token = "Bearer " + user1LoginData.get("token");

        String user2LoginResponse = user2LoginResult.getResponse().getContentAsString();
        Map<String, Object> user2LoginData = objectMapper.readValue(user2LoginResponse, new TypeReference<Map<String, Object>>() {});
        String user2Token = "Bearer " + user2LoginData.get("token");

        // Step 3: User 1 sends friend request to User 2
        String friendRequest = objectMapper.writeValueAsString(Map.of(
                "username", "frienduser2"
        ));

        mockMvc.perform(post("/api/friendships/request")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(friendRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requesterUsername").value("frienduser1"))
                .andExpect(jsonPath("$.addresseeUsername").value("frienduser2"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));

        // Step 4: User 2 views incoming requests
        mockMvc.perform(get("/api/friendships/requests/incoming")
                        .header("Authorization", user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].requesterUsername").value("frienduser1"));

        // Step 5: User 2 accepts the friend request
        mockMvc.perform(post("/api/friendships/accept/frienduser1")
                        .header("Authorization", user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // Step 6: Both users view their friends list
        mockMvc.perform(get("/api/friendships")
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].addresseeUsername").value("frienduser2"))
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));

        mockMvc.perform(get("/api/friendships")
                        .header("Authorization", user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].requesterUsername").value("frienduser1"))
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("Send Friend Request → Decline Request → Verify No Friendship")
    void testDeclineFriendshipFlow() throws Exception {
        // Register and login two users
        String user1Register = objectMapper.writeValueAsString(Map.of(
                "username", "declineuser1",
                "email", "declineuser1@test.com",
                "password", "password123",
                "name", "Decline User 1"
        ));

        String user2Register = objectMapper.writeValueAsString(Map.of(
                "username", "declineuser2",
                "email", "declineuser2@test.com",
                "password", "password123",
                "name", "Decline User 2"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Register))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Register))
                .andExpect(status().isOk());

        // Login
        MvcResult user1Login = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "declineuser1@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult user2Login = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "declineuser2@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String user1Token = "Bearer " + objectMapper.readValue(user1Login.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");
        String user2Token = "Bearer " + objectMapper.readValue(user2Login.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");

        // Send friend request
        mockMvc.perform(post("/api/friendships/request")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "declineuser2"))))
                .andExpect(status().isOk());

        // Decline request
        mockMvc.perform(delete("/api/friendships/decline/declineuser1")
                        .header("Authorization", user2Token))
                .andExpect(status().isNoContent());

        // Verify no friendship exists
        mockMvc.perform(get("/api/friendships")
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Send Friend Request → Cancel Request → Verify No Friendship")
    void testCancelFriendshipFlow() throws Exception {
        // Register and login two users
        String user1Register = objectMapper.writeValueAsString(Map.of(
                "username", "canceluser1",
                "email", "canceluser1@test.com",
                "password", "password123",
                "name", "Cancel User 1"
        ));

        String user2Register = objectMapper.writeValueAsString(Map.of(
                "username", "canceluser2",
                "email", "canceluser2@test.com",
                "password", "password123",
                "name", "Cancel User 2"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Register))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Register))
                .andExpect(status().isOk());

        // Login
        MvcResult user1Login = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "canceluser1@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String user1Token = "Bearer " + objectMapper.readValue(user1Login.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");

        // Send friend request
        mockMvc.perform(post("/api/friendships/request")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "canceluser2"))))
                .andExpect(status().isOk());

        // Cancel request
        mockMvc.perform(delete("/api/friendships/cancel/canceluser2")
                        .header("Authorization", user1Token))
                .andExpect(status().isNoContent());

        // Verify no outgoing requests
        mockMvc.perform(get("/api/friendships/requests/outgoing")
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Accept Friendship → Unfriend → Verify No Friendship")
    void testUnfriendFlow() throws Exception {
        // Register and login two users
        String user1Register = objectMapper.writeValueAsString(Map.of(
                "username", "unfrienduser1",
                "email", "unfrienduser1@test.com",
                "password", "password123",
                "name", "Unfriend User 1"
        ));

        String user2Register = objectMapper.writeValueAsString(Map.of(
                "username", "unfrienduser2",
                "email", "unfrienduser2@test.com",
                "password", "password123",
                "name", "Unfriend User 2"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user1Register))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(user2Register))
                .andExpect(status().isOk());

        // Login
        MvcResult user1Login = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "unfrienduser1@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult user2Login = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "unfrienduser2@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String user1Token = "Bearer " + objectMapper.readValue(user1Login.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");
        String user2Token = "Bearer " + objectMapper.readValue(user2Login.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");

        // Send and accept friend request
        mockMvc.perform(post("/api/friendships/request")
                        .header("Authorization", user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "unfrienduser2"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/friendships/accept/unfrienduser1")
                        .header("Authorization", user2Token))
                .andExpect(status().isOk());

        // Unfriend
        mockMvc.perform(delete("/api/friendships/unfriend/unfrienduser2")
                        .header("Authorization", user1Token))
                .andExpect(status().isNoContent());

        // Verify no friendship exists
        mockMvc.perform(get("/api/friendships")
                        .header("Authorization", user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
