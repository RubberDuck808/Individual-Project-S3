package nl.fontys.db3.backend.integration.service;

import nl.fontys.db3.backend.entity.Friendship;
import nl.fontys.db3.backend.entity.FriendshipStatus;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.FriendshipRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.FriendshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true",
    "app.jwt.secret=ci-test-secret",
    "MAPBOX_TOKEN=test"
})
@Transactional
class FriendshipServiceIT {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User alice;
    private User bob;
    private User charlie;
    private Role userRole;

    @BeforeEach
    void setUp() {
        try {
            friendshipRepository.deleteAll();
            userRepository.deleteAll();
            roleRepository.deleteAll();
        } catch (Exception ignored) {
            // Ignore exceptions during cleanup - repositories may be empty
        }

        userRole = Role.builder().name("USER").build();
        roleRepository.save(userRole);

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
    }

    @Test
    void completeFriendshipWorkflow_sendRequest_accept_unfriend() {
        Friendship request = friendshipService.sendRequest(alice.getId(), bob.getId());
        assertNotNull(request);
        assertEquals(FriendshipStatus.REQUESTED, request.getStatus());
        assertEquals(alice.getId(), request.getRequester().getId());
        assertEquals(bob.getId(), request.getAddressee().getId());

        List<Friendship> incomingRequests = friendshipService.getIncomingRequests(bob.getId());
        assertEquals(1, incomingRequests.size());
        assertEquals(alice.getId(), incomingRequests.get(0).getRequester().getId());

        List<Friendship> outgoingRequests = friendshipService.getOutgoingRequests(alice.getId());
        assertEquals(1, outgoingRequests.size());
        assertEquals(bob.getId(), outgoingRequests.get(0).getAddressee().getId());

        Friendship accepted = friendshipService.acceptIncomingFromUsername(bob.getId(), "alice");
        assertNotNull(accepted);
        assertEquals(FriendshipStatus.ACCEPTED, accepted.getStatus());

        List<Friendship> bobFriends = friendshipService.getFriends(bob.getId());
        assertEquals(1, bobFriends.size());
        assertEquals(FriendshipStatus.ACCEPTED, bobFriends.get(0).getStatus());

        List<Friendship> aliceFriends = friendshipService.getFriends(alice.getId());
        assertEquals(1, aliceFriends.size());
        assertEquals(FriendshipStatus.ACCEPTED, aliceFriends.get(0).getStatus());

        friendshipService.unfriend(bob.getId(), alice.getId());

        List<Friendship> bobFriendsAfter = friendshipService.getFriends(bob.getId());
        assertTrue(bobFriendsAfter.isEmpty());

        List<Friendship> aliceFriendsAfter = friendshipService.getFriends(alice.getId());
        assertTrue(aliceFriendsAfter.isEmpty());
    }

    @Test
    void sendRequestByUsername_success() {
        Friendship request = friendshipService.sendRequestByUsername(alice.getId(), "bob");
        assertNotNull(request);
        assertEquals(FriendshipStatus.REQUESTED, request.getStatus());
        assertEquals(alice.getId(), request.getRequester().getId());
        assertEquals(bob.getId(), request.getAddressee().getId());
    }

    @Test
    void cannotSendDuplicateRequest() {
        friendshipService.sendRequest(alice.getId(), bob.getId());
        assertThrows(IllegalArgumentException.class, this::sendDuplicateRequest);
    }

    private void sendDuplicateRequest() {
        friendshipService.sendRequest(alice.getId(), bob.getId());
    }

    @Test
    void cannotFriendYourself() {
        assertThrows(IllegalArgumentException.class, this::sendRequestToSelf);
    }

    private void sendRequestToSelf() {
        friendshipService.sendRequest(alice.getId(), alice.getId());
    }

    @Test
    void declineRequest_removesFriendship() {
        friendshipService.sendRequest(alice.getId(), bob.getId());

        List<Friendship> incomingBefore = friendshipService.getIncomingRequests(bob.getId());
        assertEquals(1, incomingBefore.size());

        friendshipService.declineIncomingFromUsername(bob.getId(), "alice");

        List<Friendship> incomingAfter = friendshipService.getIncomingRequests(bob.getId());
        assertTrue(incomingAfter.isEmpty());
    }

    @Test
    void getFriends_multipleFriends_returnsAll() {
        friendshipService.sendRequest(alice.getId(), bob.getId());
        friendshipService.acceptIncomingFromUsername(bob.getId(), "alice");

        friendshipService.sendRequest(alice.getId(), charlie.getId());
        friendshipService.acceptIncomingFromUsername(charlie.getId(), "alice");

        List<Friendship> aliceFriends = friendshipService.getFriends(alice.getId());
        assertEquals(2, aliceFriends.size());
    }
}
