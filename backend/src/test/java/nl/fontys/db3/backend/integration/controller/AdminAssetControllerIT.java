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
class AdminAssetControllerIT {

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

    private Role adminRole;
    private Role userRole;
    private User adminUser;
    private User regularUser;
    private Avatar testAvatar;
    private Background testBackground;
    private String adminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        // Create ADMIN role
        adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = Role.builder().name("ADMIN").build();
                    return roleRepository.save(role);
                });

        // Create USER role
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        // Create admin user
        adminUser = User.builder()
                .username("admin")
                .email("admin@test.com")
                .name("Admin User")
                .password(passwordEncoder.encode("password123"))
                .role(adminRole)
                .build();
        adminUser = userRepository.save(adminUser);

        // Create regular user
        regularUser = User.builder()
                .username("regularuser")
                .email("regular@test.com")
                .name("Regular User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        regularUser = userRepository.save(regularUser);

        // Create test avatar
        testAvatar = Avatar.builder()
                .name("test-avatar")
                .imagePath("/avatars/test.png")
                .active(true)
                .build();
        testAvatar = avatarRepository.save(testAvatar);

        // Create test background
        testBackground = Background.builder()
                .name("test-background")
                .imagePath("/backgrounds/test.png")
                .active(true)
                .build();
        testBackground = backgroundRepository.save(testBackground);

        // Generate tokens
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin@test.com");
        adminToken = jwtService.generateToken(adminDetails.getUsername(), Map.of());

        UserDetails userDetails = userDetailsService.loadUserByUsername("regular@test.com");
        regularUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());
    }

    // Avatar tests
    @Test
    void getAllAvatars_success() throws Exception {
        mockMvc.perform(get("/api/admin/assets/avatars")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllAvatars_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/assets/avatars")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAvatarById_success() throws Exception {
        mockMvc.perform(get("/api/admin/assets/avatars/" + testAvatar.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAvatar.getId()));
    }

    @Test
    void createAvatar_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "new-avatar",
                "imagePath", "/avatars/new.png"
        ));

        mockMvc.perform(post("/api/admin/assets/avatars")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new-avatar"));
    }

    @Test
    void updateAvatar_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "updated-avatar",
                "imagePath", "/avatars/updated.png",
                "active", true
        ));

        mockMvc.perform(put("/api/admin/assets/avatars/" + testAvatar.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-avatar"));
    }

    @Test
    void deleteAvatar_success() throws Exception {
        mockMvc.perform(delete("/api/admin/assets/avatars/" + testAvatar.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateAvatar_success() throws Exception {
        mockMvc.perform(put("/api/admin/assets/avatars/" + testAvatar.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // Background tests
    @Test
    void getAllBackgrounds_success() throws Exception {
        mockMvc.perform(get("/api/admin/assets/backgrounds")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllBackgrounds_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/assets/backgrounds")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBackgroundById_success() throws Exception {
        mockMvc.perform(get("/api/admin/assets/backgrounds/" + testBackground.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBackground.getId()));
    }

    @Test
    void createBackground_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "new-background",
                "imagePath", "/backgrounds/new.png"
        ));

        mockMvc.perform(post("/api/admin/assets/backgrounds")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("new-background"));
    }

    @Test
    void updateBackground_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "name", "updated-background",
                "imagePath", "/backgrounds/updated.png",
                "active", true
        ));

        mockMvc.perform(put("/api/admin/assets/backgrounds/" + testBackground.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updated-background"));
    }

    @Test
    void deleteBackground_success() throws Exception {
        mockMvc.perform(delete("/api/admin/assets/backgrounds/" + testBackground.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateBackground_success() throws Exception {
        mockMvc.perform(put("/api/admin/assets/backgrounds/" + testBackground.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
