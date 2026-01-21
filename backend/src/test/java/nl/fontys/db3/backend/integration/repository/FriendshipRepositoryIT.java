package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.Friendship;
import nl.fontys.db3.backend.entity.FriendshipStatus;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.FriendshipRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class FriendshipRepositoryIT {

    @Autowired
    FriendshipRepository friendshipRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    private User alice;
    private User bob;
    private User charlie;

    @BeforeEach
    void setUp() {
        try {
            friendshipRepository.deleteAll();
            userRepository.deleteAll();
            // Don't delete roles - migration V4__Seed_roles.sql creates them
        } catch (Exception ignored) {
            // Tables may not exist yet
        }

        // Use existing USER role from migration
        Role role = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        alice = User.builder()
                .username("alice")
                .email("alice@test.com")
                .name("Alice")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(alice);

        bob = User.builder()
                .username("bob")
                .email("bob@test.com")
                .name("Bob")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(bob);

        charlie = User.builder()
                .username("charlie")
                .email("charlie@test.com")
                .name("Charlie")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(charlie);
    }

    @Test
    void saveAndFindById() {
        Friendship friendship = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        Friendship saved = friendshipRepository.save(friendship);
        assertNotNull(saved.getId());

        Friendship found = friendshipRepository.findById(saved.getId()).orElseThrow();
        assertEquals(alice.getId(), found.getRequester().getId());
        assertEquals(bob.getId(), found.getAddressee().getId());
        assertEquals(FriendshipStatus.REQUESTED, found.getStatus());
    }

    @Test
    void findByAddressee_IdAndStatus() {
        Friendship request1 = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(request1);

        Friendship request2 = Friendship.builder()
                .requester(charlie)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(request2);

        Friendship accepted = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(accepted);

        List<Friendship> incomingRequests = friendshipRepository.findByAddressee_IdAndStatus(
                bob.getId(), FriendshipStatus.REQUESTED
        );
        assertEquals(2, incomingRequests.size());
    }

    @Test
    void findByRequester_IdAndStatus() {
        Friendship request1 = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(request1);

        Friendship request2 = Friendship.builder()
                .requester(alice)
                .addressee(charlie)
                .status(FriendshipStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(request2);

        List<Friendship> outgoingRequests = friendshipRepository.findByRequester_IdAndStatus(
                alice.getId(), FriendshipStatus.REQUESTED
        );
        assertEquals(2, outgoingRequests.size());
    }

    @Test
    void findByStatusAndRequester_IdOrStatusAndAddressee_Id() {
        Friendship friendship1 = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(friendship1);

        Friendship friendship2 = Friendship.builder()
                .requester(bob)
                .addressee(charlie)
                .status(FriendshipStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(friendship2);

        List<Friendship> aliceFriendships = friendshipRepository.findByStatusAndRequester_IdOrStatusAndAddressee_Id(
                FriendshipStatus.ACCEPTED, alice.getId(),
                FriendshipStatus.ACCEPTED, alice.getId()
        );
        assertEquals(1, aliceFriendships.size());
    }

    @Test
    void findBetweenUsers_Exists() {
        Friendship friendship = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(friendship);

        Optional<Friendship> found = friendshipRepository.findBetweenUsers(alice.getId(), bob.getId());
        assertTrue(found.isPresent());
        assertEquals(friendship.getId(), found.get().getId());
    }

    @Test
    void findBetweenUsers_ReverseOrder() {
        Friendship friendship = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .createdAt(LocalDateTime.now())
                .build();
        friendshipRepository.save(friendship);

        Optional<Friendship> found = friendshipRepository.findBetweenUsers(bob.getId(), alice.getId());
        assertTrue(found.isPresent());
        assertEquals(friendship.getId(), found.get().getId());
    }

    @Test
    void findBetweenUsers_NotExists() {
        Optional<Friendship> found = friendshipRepository.findBetweenUsers(alice.getId(), bob.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void updateStatus() {
        Friendship friendship = Friendship.builder()
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();
        Friendship saved = friendshipRepository.save(friendship);

        saved.setStatus(FriendshipStatus.ACCEPTED);
        Friendship updated = friendshipRepository.save(saved);

        assertEquals(FriendshipStatus.ACCEPTED, updated.getStatus());
    }
}
