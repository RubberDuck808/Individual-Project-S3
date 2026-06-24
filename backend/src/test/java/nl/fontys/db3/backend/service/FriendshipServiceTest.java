package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Friendship;
import nl.fontys.db3.backend.entity.FriendshipStatus;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.FriendshipRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().id(1L).name("USER").build();
        
        alice = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@test.com")
                .name("Alice")
                .password("encoded")
                .role(userRole)
                .build();

        bob = User.builder()
                .id(2L)
                .username("bob")
                .email("bob@test.com")
                .name("Bob")
                .password("encoded")
                .role(userRole)
                .build();
    }

    @Test
    void sendRequest_success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> {
            Friendship f = invocation.getArgument(0);
            f.setId(1L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        // When
        Friendship result = friendshipService.sendRequest(1L, 2L);

        // Then
        assertNotNull(result);
        assertEquals(FriendshipStatus.REQUESTED, result.getStatus());
        assertEquals(1L, result.getRequester().getId());
        assertEquals(2L, result.getAddressee().getId());
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(friendshipRepository).findBetweenUsers(1L, 2L);
        verify(friendshipRepository).save(any(Friendship.class));
    }


    @Test
    void sendRequest_duplicateRequestThrows() {
        // Given
        Friendship existing = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existing));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.sendRequest(1L, 2L);
        });
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequestByUsername_success() {
        // Given
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> {
            Friendship f = invocation.getArgument(0);
            f.setId(1L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        // When
        Friendship result = friendshipService.sendRequestByUsername(1L, "bob");

        // Then
        assertNotNull(result);
        assertEquals(FriendshipStatus.REQUESTED, result.getStatus());
        verify(userRepository).findByUsername("bob");
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void sendRequestByUsername_usernameNormalized() {
        // Given - service normalizes "  BOB  " to "bob" by trimming and lowercasing
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> {
            Friendship f = invocation.getArgument(0);
            f.setId(1L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        // When - username has whitespace and mixed case (should be normalized to "bob")
        friendshipService.sendRequestByUsername(1L, "  BOB  ");

        // Then - verify username was normalized (trimmed and lowercased) before lookup
        verify(userRepository).findByUsername("bob");
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void sendRequestByUsername_userNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.sendRequestByUsername(1L, "nonexistent");
        });
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void acceptIncomingFromUsername_success() {
        // Given
        Friendship request = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(friendshipRepository.findBetweenUsers(2L, 1L)).thenReturn(Optional.of(request));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Friendship result = friendshipService.acceptIncomingFromUsername(2L, "alice");

        // Then
        assertNotNull(result);
        assertEquals(FriendshipStatus.ACCEPTED, result.getStatus());
        verify(friendshipRepository).save(request);
    }

    @Test
    void acceptIncomingFromUsername_requestNotFound() {
        // Given
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(friendshipRepository.findBetweenUsers(2L, 1L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.acceptIncomingFromUsername(2L, "alice");
        });
        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void acceptIncomingFromUsername_alreadyAccepted() {
        // Given
        Friendship accepted = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(friendshipRepository.findBetweenUsers(2L, 1L)).thenReturn(Optional.of(accepted));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.acceptIncomingFromUsername(2L, "alice");
        });
    }

    @Test
    void declineIncomingFromUsername_success() {
        // Given
        Friendship request = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(friendshipRepository.findBetweenUsers(2L, 1L)).thenReturn(Optional.of(request));

        // When
        friendshipService.declineIncomingFromUsername(2L, "alice");

        // Then
        verify(friendshipRepository).delete(request);
    }

    @Test
    void unfriend_success() {
        // Given
        Friendship friendship = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        // When
        friendshipService.unfriend(1L, 2L);

        // Then
        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void unfriend_friendshipNotFound() {
        // Given
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.unfriend(1L, 2L);
        });
        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void unfriend_notFriends() {
        // Given
        Friendship request = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .build();

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(request));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            friendshipService.unfriend(1L, 2L);
        });
        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void getIncomingRequests() {
        // Given
        Friendship request = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .build();

        List<Friendship> requests = List.of(request);
        when(friendshipRepository.findByAddressee_IdAndStatus(2L, FriendshipStatus.REQUESTED))
                .thenReturn(requests);

        // When
        List<Friendship> result = friendshipService.getIncomingRequests(2L);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(friendshipRepository).findByAddressee_IdAndStatus(2L, FriendshipStatus.REQUESTED);
    }

    @Test
    void getOutgoingRequests() {
        // Given
        Friendship request = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.REQUESTED)
                .build();

        List<Friendship> requests = List.of(request);
        when(friendshipRepository.findByRequester_IdAndStatus(1L, FriendshipStatus.REQUESTED))
                .thenReturn(requests);

        // When
        List<Friendship> result = friendshipService.getOutgoingRequests(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(friendshipRepository).findByRequester_IdAndStatus(1L, FriendshipStatus.REQUESTED);
    }

    @Test
    void getFriends() {
        // Given
        Friendship friendship = Friendship.builder()
                .id(1L)
                .requester(alice)
                .addressee(bob)
                .status(FriendshipStatus.ACCEPTED)
                .build();

        List<Friendship> friends = List.of(friendship);
        when(friendshipRepository.findByStatusAndRequester_IdOrStatusAndAddressee_Id(
                FriendshipStatus.ACCEPTED, 1L,
                FriendshipStatus.ACCEPTED, 1L))
                .thenReturn(friends);

        // When
        List<Friendship> result = friendshipService.getFriends(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(FriendshipStatus.ACCEPTED, result.get(0).getStatus());
        verify(friendshipRepository).findByStatusAndRequester_IdOrStatusAndAddressee_Id(
                FriendshipStatus.ACCEPTED, 1L,
                FriendshipStatus.ACCEPTED, 1L);
    }
}
