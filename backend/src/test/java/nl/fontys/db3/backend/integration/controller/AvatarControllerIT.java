package nl.fontys.db3.backend.integration.controller;

import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.AvatarRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AvatarControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvatarRepository avatarRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Avatar activeAvatar1;
    private Avatar activeAvatar2;
    private Avatar inactiveAvatar;
    private Role userRole;
    private User testUser;
    private String testUserToken;

    @BeforeEach
    void setUp() {
        // Create user role
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .name("Test User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);

        // Generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");
        testUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());

        // Create active avatars
        activeAvatar1 = Avatar.builder()
                .name("avatar1")
                .imagePath("/avatars/avatar1.png")
                .active(true)
                .build();
        activeAvatar1 = avatarRepository.save(activeAvatar1);

        activeAvatar2 = Avatar.builder()
                .name("avatar2")
                .imagePath("/avatars/avatar2.png")
                .active(true)
                .build();
        activeAvatar2 = avatarRepository.save(activeAvatar2);

        // Create inactive avatar (should not appear in results)
        inactiveAvatar = Avatar.builder()
                .name("inactive-avatar")
                .imagePath("/avatars/inactive.png")
                .active(false)
                .build();
        inactiveAvatar = avatarRepository.save(inactiveAvatar);
    }

    @Test
    void getActiveAvatars_success() throws Exception {
        mockMvc.perform(get("/api/avatars")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.containsInAnyOrder(
                        activeAvatar1.getId().intValue(), activeAvatar2.getId().intValue())))
                .andExpect(jsonPath("$[*].name").value(org.hamcrest.Matchers.containsInAnyOrder("avatar1", "avatar2")))
                .andExpect(jsonPath("$[*].active").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(true))));
    }

    @Test
    void getActiveAvatars_noActiveAvatars() throws Exception {
        // Delete all avatars
        avatarRepository.deleteAll();

        mockMvc.perform(get("/api/avatars")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getActiveAvatars_onlyInactiveAvatars() throws Exception {
        // Delete active avatars, keep only inactive
        avatarRepository.delete(activeAvatar1);
        avatarRepository.delete(activeAvatar2);

        mockMvc.perform(get("/api/avatars")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
