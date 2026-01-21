package nl.fontys.db3.backend.integration.controller;

import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.BackgroundRepository;
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
class BackgroundControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BackgroundRepository backgroundRepository;

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

    private Background activeBackground1;
    private Background activeBackground2;
    private Background inactiveBackground;
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

        // Create active backgrounds
        activeBackground1 = Background.builder()
                .name("background1")
                .imagePath("/backgrounds/bg1.png")
                .active(true)
                .build();
        activeBackground1 = backgroundRepository.save(activeBackground1);

        activeBackground2 = Background.builder()
                .name("background2")
                .imagePath("/backgrounds/bg2.png")
                .active(true)
                .build();
        activeBackground2 = backgroundRepository.save(activeBackground2);

        // Create inactive background (should not appear in results)
        inactiveBackground = Background.builder()
                .name("inactive-background")
                .imagePath("/backgrounds/inactive.png")
                .active(false)
                .build();
        inactiveBackground = backgroundRepository.save(inactiveBackground);
    }

    @Test
    void getActiveBackgrounds_success() throws Exception {
        mockMvc.perform(get("/api/backgrounds")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.containsInAnyOrder(
                        activeBackground1.getId().intValue(), activeBackground2.getId().intValue())))
                .andExpect(jsonPath("$[*].name").value(org.hamcrest.Matchers.containsInAnyOrder("background1", "background2")))
                .andExpect(jsonPath("$[*].active").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(true))));
    }

    @Test
    void getActiveBackgrounds_noActiveBackgrounds() throws Exception {
        // Delete all backgrounds
        backgroundRepository.deleteAll();

        mockMvc.perform(get("/api/backgrounds")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getActiveBackgrounds_onlyInactiveBackgrounds() throws Exception {
        // Delete active backgrounds, keep only inactive
        backgroundRepository.delete(activeBackground1);
        backgroundRepository.delete(activeBackground2);

        mockMvc.perform(get("/api/backgrounds")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
