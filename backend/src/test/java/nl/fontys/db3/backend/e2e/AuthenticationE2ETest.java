package nl.fontys.db3.backend.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("E2E: Authentication Flow")
class AuthenticationE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        setUpBase();
    }

    @Test
    @DisplayName("Register User → Login → Access Protected Endpoint → Verify Authentication")
    void testAuthenticationFlow() throws Exception {
        // Step 1: Register
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "authuser",
                "email", "authuser@test.com",
                "password", "password123",
                "name", "Auth User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("authuser"));

        // Step 2: Try to access protected endpoint without token (should fail)
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        // Step 3: Login
        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "authuser@test.com",
                "password", "password123"
        ));

        var loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("authuser"))
                .andReturn();

        String token = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Step 4: Access protected endpoint with token (should succeed)
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("authuser"));
    }

    @Test
    @DisplayName("Register → Login with Wrong Password → Verify Failure")
    void testInvalidLoginFlow() throws Exception {
        // Register
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "invaliduser",
                                "email", "invaliduser@test.com",
                                "password", "password123",
                                "name", "Invalid User"
                        ))))
                .andExpect(status().isOk());

        // Try to login with wrong password
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "invaliduser@test.com",
                                "password", "wrongpassword"
                        ))))
                .andExpect(status().isUnauthorized());
    }
}
