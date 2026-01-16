package nl.fontys.db3.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.entity.Vote;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.repository.VoteRepository;
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
class VoteControllerIT {

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
    private VoteRepository voteRepository;

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
    private User hazardCreator;
    private HazardCategory category;
    private HazardReport hazard;
    private String testUserToken;

    @BeforeEach
    void setUp() {
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        testUser = User.builder()
                .username("voter")
                .email("voter@test.com")
                .name("Voter User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);

        // Create a different user to create the hazard (users can't vote on their own hazards)
        hazardCreator = User.builder()
                .username("creator")
                .email("creator@test.com")
                .name("Hazard Creator")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        hazardCreator = userRepository.save(hazardCreator);

        category = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.png")
                .active(true)
                .build();
        category = hazardCategoryRepository.save(category);

        hazard = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(hazardCreator)
                .status(nl.fontys.db3.backend.entity.HazardStatus.OPEN)
                .build();
        hazard = hazardReportRepository.save(hazard);

        UserDetails userDetails = userDetailsService.loadUserByUsername("voter@test.com");
        testUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());
    }

    @Test
    void getAllVotes_success() throws Exception {
        // Create some votes
        Vote vote1 = Vote.builder()
                .user(testUser)
                .hazardReport(hazard)
                .voteType(VoteType.UPVOTE)
                .build();
        voteRepository.save(vote1);

        mockMvc.perform(get("/api/votes")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void voteJson_upvote_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "hazardId", hazard.getId(),
                "voteType", "UPVOTE"
        ));

        mockMvc.perform(post("/api/votes")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteType").value("UPVOTE"));
    }

    @Test
    void voteJson_downvote_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "hazardId", hazard.getId(),
                "voteType", "DOWNVOTE"
        ));

        mockMvc.perform(post("/api/votes")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteType").value("DOWNVOTE"));
    }

    @Test
    void voteJson_invalidVoteType() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "hazardId", hazard.getId(),
                "voteType", "INVALID"
        ));

        mockMvc.perform(post("/api/votes")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void voteJson_unauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "hazardId", hazard.getId(),
                "voteType", "UPVOTE"
        ));

        mockMvc.perform(post("/api/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getVotes_success() throws Exception {
        // Create votes
        Vote upvote = Vote.builder()
                .user(testUser)
                .hazardReport(hazard)
                .voteType(VoteType.UPVOTE)
                .build();
        voteRepository.save(upvote);

        User anotherUser = User.builder()
                .username("another")
                .email("another@test.com")
                .name("Another User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        anotherUser = userRepository.save(anotherUser);

        Vote downvote = Vote.builder()
                .user(anotherUser)
                .hazardReport(hazard)
                .voteType(VoteType.DOWNVOTE)
                .build();
        voteRepository.save(downvote);

        mockMvc.perform(get("/api/votes/" + hazard.getId() + "/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(1))
                .andExpect(jsonPath("$.downvotes").value(1));
    }

    @Test
    void myVote_success() throws Exception {
        // Create a vote
        Vote vote = Vote.builder()
                .user(testUser)
                .hazardReport(hazard)
                .voteType(VoteType.UPVOTE)
                .build();
        voteRepository.save(vote);

        mockMvc.perform(get("/api/votes/" + hazard.getId() + "/mine")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteType").value("UPVOTE"));
    }

    @Test
    void myVote_noVote() throws Exception {
        mockMvc.perform(get("/api/votes/" + hazard.getId() + "/mine")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voteType").value("NONE"));
    }
}
