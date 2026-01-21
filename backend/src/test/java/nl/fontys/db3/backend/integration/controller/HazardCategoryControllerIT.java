package nl.fontys.db3.backend.integration.controller;

import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
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
class HazardCategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HazardCategoryRepository categoryRepository;

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

    private HazardCategory category1;
    private HazardCategory category2;
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

        // Create test categories
        category1 = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.png")
                .active(true)
                .build();
        category1 = categoryRepository.save(category1);

        category2 = HazardCategory.builder()
                .name("Construction")
                .iconPath("/icons/construction.png")
                .active(true)
                .build();
        category2 = categoryRepository.save(category2);
    }

    @Test
    void getAllCategories_success() throws Exception {
        mockMvc.perform(get("/api/hazard-categories")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[*].name").exists());
    }

    @Test
    void getAllCategories_unauthorized() throws Exception {
        mockMvc.perform(get("/api/hazard-categories"))
                .andExpect(status().isUnauthorized());
    }
}
