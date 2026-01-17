package nl.fontys.db3.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fontys.db3.backend.entity.Friendship;
import nl.fontys.db3.backend.entity.FriendshipStatus;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.FriendshipRepository;
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
class FriendshipControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private User alice;
    private User bob;
    private User charlie;
    private Role userRole;
    private String aliceToken;

    @BeforeEach
    void setUp() {
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        alice = User.builder()
                .username("alice")
                .email("alice@test.com")
                .name("Alice")
                .password("encoded")
                .role(userRole)
                .build();
        alice = userRepository.save(alice);

        bob = User.builder()
                .username("bob")
                .email("bob@test.com")
                .name("Bob")
                .password("encoded")
                .role(userRole)
                .build();
        bob = userRepository.save(bob);

        charlie = User.builder()
                .username("charlie")
                .email("charlie@test.com")
                .name("Charlie")
                .password("encoded")
                .role(userRole)
                .build();
        charlie = userRepository.save(charlie);

        UserDetails aliceDetails = userDetailsService.loadUserByUsername("alice@test.com");
        aliceToken = jwtService.generateToken(aliceDetails.getUsername(), Map.of());
    }

    @Test
    void sendRequest_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("username", "bob"));

        mockMvc.perform(post("/api/friendships/request")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requesterUsername").value("alice"))
                .andExpect(jsonPath("$.addresseeUsername").value("bob"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void sendRequest_unauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("username", "bob"));

        mockMvc.perform(post("/api/friendships/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptRequest_success() throws Exception {
        // First send a request
        Friendship request = Friendship.builder()
                .requester(bob)
                .addressee(alice)
                .status(FriendshipStatus.REQUESTED)
                .build();
        friendshipRepository.save(request);

        mockMvc.perform(post("/api/friendships/accept/bob")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void declineRequest_success() throws Exception {
        // First send a request
        Friendship request = Friendship.builder()
                .requester(bob)
                .addressee(alice)
                .status(FriendshipStatus.REQUESTED)
                .build();
        friendshipRepository.save(request);

        mockMvc.perform(delete("/api/friendships/decline/bob")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelRequest_success() throws Exception {
        // First send a request
        Friendship request = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .build();
        friendshipRepository.save(request);

        mockMvc.perform(delete("/api/friendships/cancel/bob")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void unfriend_success() throws Exception {
        // First create an accepted friendship
        Friendship friendship = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .build();
        friendshipRepository.save(friendship);

        mockMvc.perform(delete("/api/friendships/unfriend/bob")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void getIncomingRequests_success() throws Exception {
        // Create incoming requests
        Friendship request1 = Friendship.builder()
                .requester(bob)
                .addressee(alice)
                .status(FriendshipStatus.REQUESTED)
                .build();
        friendshipRepository.save(request1);

        Friendship request2 = Friendship.builder()
                .requester(charlie)
                .addressee(alice)
                .status(FriendshipStatus.REQUESTED)
                .build();
        friendshipRepository.save(request2);

        mockMvc.perform(get("/api/friendships/requests/incoming")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOutgoingRequests_success() throws Exception {
        // Create outgoing requests
        Friendship request1 = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .build();
        friendshipRepository.save(request1);

        Friendship request2 = Friendship.builder()
                .requester(alice)
                .addressee(charlie)
                .status(FriendshipStatus.REQUESTED)
                .build();
        friendshipRepository.save(request2);

        mockMvc.perform(get("/api/friendships/requests/outgoing")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getFriends_success() throws Exception {
        // Create accepted friendships
        Friendship friendship1 = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .build();
        friendshipRepository.save(friendship1);

        Friendship friendship2 = Friendship.builder()
                .requester(charlie)
                .addressee(alice)
                .status(FriendshipStatus.ACCEPTED)
                .build();
        friendshipRepository.save(friendship2);

        mockMvc.perform(get("/api/friendships")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getFriendsOfUser_success() throws Exception {
        // Create accepted friendships for bob
        Friendship friendship1 = Friendship.builder()
                .requester(bob)
                .addressee(alice)
                .status(FriendshipStatus.ACCEPTED)
                .build();
        friendshipRepository.save(friendship1);

        Friendship friendship2 = Friendship.builder()
                .requester(bob)
                .addressee(charlie)
                .status(FriendshipStatus.ACCEPTED)
                .build();
        friendshipRepository.save(friendship2);

        mockMvc.perform(get("/api/friendships/user/bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
