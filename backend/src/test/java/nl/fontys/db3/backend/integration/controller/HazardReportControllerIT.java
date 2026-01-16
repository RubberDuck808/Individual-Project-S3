package nl.fontys.db3.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true",
    "app.jwt.secret=ci-test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha-algorithms",
    "MAPBOX_TOKEN=test"
})
@Transactional
class HazardReportControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private HazardCategoryRepository hazardCategoryRepository;

    @Autowired
    private HazardReportRepository hazardReportRepository;

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
    private HazardCategory category;
    private String testUserToken;

    @BeforeEach
    void setUp() {
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        testUser = User.builder()
                .username("reporter")
                .email("reporter@test.com")
                .name("Reporter User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);

        category = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.png")
                .active(true)
                .build();
        category = hazardCategoryRepository.save(category);

        UserDetails userDetails = userDetailsService.loadUserByUsername("reporter@test.com");
        testUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());
    }

    @Test
    void getOpenHazards_success() throws Exception {
        // Create open hazards
        HazardReport hazard1 = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard1);

        HazardReport hazard2 = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard2);

        // Create a resolved hazard (should not appear)
        HazardReport resolvedHazard = HazardReport.builder()
                .latitude(51.4600)
                .longitude(5.4900)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.RESOLVED)
                .build();
        hazardReportRepository.save(resolvedHazard);

        mockMvc.perform(get("/api/hazards/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createHazard_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "latitude", 51.4416,
                "longitude", 5.4697,
                "categoryId", category.getId()
        ));

        mockMvc.perform(post("/api/hazards")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(51.4416))
                .andExpect(jsonPath("$.longitude").value(5.4697))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createHazard_unauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "latitude", 51.4416,
                "longitude", 5.4697,
                "categoryId", category.getId()
        ));

        mockMvc.perform(post("/api/hazards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getHazardsByUser_success() throws Exception {
        // Create hazards for test user
        HazardReport hazard1 = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard1);

        HazardReport hazard2 = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.VERIFIED)
                .build();
        hazardReportRepository.save(hazard2);

        // Create hazard for another user
        User anotherUser = User.builder()
                .username("another")
                .email("another@test.com")
                .name("Another User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        anotherUser = userRepository.save(anotherUser);

        HazardReport otherHazard = HazardReport.builder()
                .latitude(51.4600)
                .longitude(5.4900)
                .category(category)
                .createdBy(anotherUser)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(otherHazard);

        mockMvc.perform(get("/api/hazards/by-user/reporter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getHazardsByUser_userNotFound() throws Exception {
        mockMvc.perform(get("/api/hazards/by-user/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
