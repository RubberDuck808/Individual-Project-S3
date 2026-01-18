package nl.fontys.db3.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.AvatarRepository;
import nl.fontys.db3.backend.repository.BackgroundRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AvatarRepository avatarRepository;

    @Autowired
    private BackgroundRepository backgroundRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Role userRole;
    private User testUser;
    private String testUserToken;

    @BeforeEach
    void setUp() {
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        testUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .name("Test User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);

        // Create test avatars and backgrounds
        Avatar avatar1 = Avatar.builder()
                .name("avatar1")
                .imagePath("/avatars/avatar1.png")
                .active(true)
                .build();
        avatarRepository.save(avatar1);

        Background bg1 = Background.builder()
                .name("bg1")
                .imagePath("/backgrounds/bg1.png")
                .active(true)
                .build();
        backgroundRepository.save(bg1);

        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");
        testUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());
    }

    @Test
    void register_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "username", "newuser",
                "email", "newuser@test.com",
                "password", "password123",
                "name", "New User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@test.com"))
                .andExpect(jsonPath("$.name").value("New User"));
    }

    @Test
    void register_duplicateEmail() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "username", "anotheruser",
                "email", "test@test.com", // Duplicate email
                "password", "password123",
                "name", "Another User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "email", "test@test.com",
                "password", "password123"
        ));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void login_invalidCredentials() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "email", "test@test.com",
                "password", "wrongpassword"
        ));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_success() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void getCurrentUser_unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMe_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "Updated Name",
                "username", "testuser",
                "email", "test@test.com",
                "currentPassword", "password123",
                "newPassword", ""
        ));

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateMe_unauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "Updated Name",
                "username", "testuser",
                "email", "test@test.com"
        ));

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getByUsername_success() throws Exception {
        mockMvc.perform(get("/api/users/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void getByUsername_notFound() throws Exception {
        mockMvc.perform(get("/api/users/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeMyAvatar_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("avatarName", "avatar1"));

        mockMvc.perform(put("/api/users/me/avatar")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void changeMyBackground_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("backgroundName", "bg1"));

        mockMvc.perform(put("/api/users/me/background")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void changeMyAvatar_unauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("avatarName", "avatar1"));

        mockMvc.perform(put("/api/users/me/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changeMyBackground_unauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("backgroundName", "bg1"));

        mockMvc.perform(put("/api/users/me/background")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_missingFields() throws Exception {
        // Missing required fields
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "username", "newuser"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_missingPassword() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "email", "test@test.com"
        ));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_invalidEmail() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "email", "nonexistent@test.com",
                "password", "password123"
        ));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMe_invalidCurrentPassword() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "Updated Name",
                "currentPassword", "wrongpassword",
                "newPassword", "newpassword123"
        ));

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeMyAvatar_avatarNotFound() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("avatarName", "nonexistent"));

        mockMvc.perform(put("/api/users/me/avatar")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError());
    }
}
