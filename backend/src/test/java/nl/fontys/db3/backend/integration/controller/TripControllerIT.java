package nl.fontys.db3.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
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

import java.time.OffsetDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TripControllerIT {

    @Autowired
    private MockMvc mockMvc;

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
                .username("tripper")
                .email("tripper@test.com")
                .name("Tripper User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername("tripper@test.com");
        testUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());
    }

    @Test
    void completeTrip_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "startLat", 51.4416,
                "startLng", 5.4697,
                "endLat", 51.4500,
                "endLng", 5.4800,
                "distanceKm", 10.5,
                "startedAt", OffsetDateTime.now().minusHours(1).toString(),
                "endedAt", OffsetDateTime.now().toString()
        ));

        mockMvc.perform(post("/api/trips/complete")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(10.5))
                .andExpect(jsonPath("$.startLat").value(51.4416))
                .andExpect(jsonPath("$.endLat").value(51.4500));
    }

    @Test
    void completeTrip_unauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "startLat", 51.4416,
                "startLng", 5.4697,
                "endLat", 51.4500,
                "endLng", 5.4800,
                "distanceKm", 10.5,
                "startedAt", OffsetDateTime.now().minusHours(1).toString(),
                "endedAt", OffsetDateTime.now().toString()
        ));

        mockMvc.perform(post("/api/trips/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void completeTrip_invalidDistance() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "startLat", 51.4416,
                "startLng", 5.4697,
                "endLat", 51.4500,
                "endLng", 5.4800,
                "distanceKm", -5.0, // Invalid negative distance
                "startedAt", OffsetDateTime.now().minusHours(1).toString(),
                "endedAt", OffsetDateTime.now().toString()
        ));

        mockMvc.perform(post("/api/trips/complete")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeTrip_missingFields() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "startLat", 51.4416,
                "startLng", 5.4697
                // Missing required fields
        ));

        mockMvc.perform(post("/api/trips/complete")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
