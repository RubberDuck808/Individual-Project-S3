package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Friendship;
import nl.fontys.db3.backend.entity.FriendshipStatus;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.FriendshipRepository;
import nl.fontys.db3.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

    @Mock private FriendshipRepository friendshipRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private FriendshipService service;

    /* ===================== sendRequest ===================== */

    @Test
    void sendRequest_sameIds_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.sendRequest(1L, 1L));
        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void sendRequest_requesterNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.sendRequest(1L, 2L));

        verify(userRepository).findById(1L);
        verify(userRepository, never()).findById(2L);
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void sendRequest_addresseeNotFound_throws() {
        User requester = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.sendRequest(1L, 2L));

        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void sendRequest_alreadyFriends_throws() {
        User requester = mock(User.class);
        User addressee = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(addressee));

        Friendship existing = mock(Friendship.class);
        when(existing.getStatus()).thenReturn(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.sendRequest(1L, 2L));

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequest_requestAlreadyExists_throws() {
        User requester = mock(User.class);
        User addressee = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(addressee));

        Friendship existing = mock(Friendship.class);
        when(existing.getStatus()).thenReturn(FriendshipStatus.REQUESTED);
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.sendRequest(1L, 2L));

        verify(friendshipRepository, never()).save(any());
    }

    @Test
    void sendRequest_success_savesRequestedFriendship() {
        User requester = mock(User.class);
        User addressee = mock(User.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(addressee));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());

        ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
        when(friendshipRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        Friendship saved = service.sendRequest(1L, 2L);

        assertNotNull(saved);
        Friendship toSave = captor.getValue();
        assertSame(requester, toSave.getRequester());
        assertSame(addressee, toSave.getAddressee());
        assertEquals(FriendshipStatus.REQUESTED, toSave.getStatus());
        assertNotNull(toSave.getCreatedAt());

        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void sendRequest_dataIntegrityViolation_mapsToAlreadyExists() {
        User requester = mock(User.class);
        User addressee = mock(User.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(addressee));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());

        when(friendshipRepository.save(any(Friendship.class)))
                .thenThrow(new DataIntegrityViolationException("dup"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.sendRequest(1L, 2L));

        assertEquals("Friend request already exists", ex.getMessage());
    }

    /* ===================== unfriend ===================== */

    @Test
    void unfriend_friendshipNotFound_throws() {
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.unfriend(1L, 2L));

        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void unfriend_notAccepted_throws() {
        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.REQUESTED);
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(IllegalArgumentException.class, () -> service.unfriend(1L, 2L));

        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void unfriend_success_deletesFriendship() {
        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        service.unfriend(1L, 2L);

        verify(friendshipRepository).delete(friendship);
    }

    /* ===================== sendRequestByUsername ===================== */

    @Test
    void sendRequestByUsername_blankUsername_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.sendRequestByUsername(1L, "   "));
        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void sendRequestByUsername_userNotFound_throws() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.sendRequestByUsername(1L, "bob"));

        verify(userRepository).findByUsername("bob");
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void sendRequestByUsername_success_delegatesToSendRequest() {
        User addressee = mock(User.class);
        when(addressee.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(addressee));

        // now sendRequest(1,2) happens:
        User requester = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepository.findById(2L)).thenReturn(Optional.of(addressee));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship result = service.sendRequestByUsername(1L, "bob");

        assertNotNull(result);
        verify(userRepository).findByUsername("bob");
        verify(friendshipRepository).save(any(Friendship.class));
    }

    /* ===================== unfriendByUsername ===================== */

    @Test
    void unfriendByUsername_userNotFound_throws() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.unfriendByUsername(1L, "bob"));
    }

    @Test
    void unfriendByUsername_success_delegatesToUnfriend() {
        User other = mock(User.class);
        when(other.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(other));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        service.unfriendByUsername(1L, "bob");

        verify(friendshipRepository).delete(friendship);
    }

    /* ===================== acceptIncomingFromUsername ===================== */

    @Test
    void acceptIncomingFromUsername_requesterNotFound_throws() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.acceptIncomingFromUsername(1L, "bob"));
    }

    @Test
    void acceptIncomingFromUsername_requestNotFound_throws() {
        User requester = mock(User.class);
        when(requester.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(requester));
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.acceptIncomingFromUsername(1L, "bob"));
    }

    @Test
    void acceptIncomingFromUsername_notRequested_throws() {
        User requester = mock(User.class);
        when(requester.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(requester));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(IllegalArgumentException.class,
                () -> service.acceptIncomingFromUsername(1L, "bob"));
    }

    @Test
    void acceptIncomingFromUsername_notIncoming_throws() {
        User requester = mock(User.class);
        when(requester.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(requester));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.REQUESTED);

        // addressee is NOT meId
        User addressee = mock(User.class);
        when(addressee.getId()).thenReturn(999L);
        when(friendship.getAddressee()).thenReturn(addressee);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(IllegalArgumentException.class,
                () -> service.acceptIncomingFromUsername(1L, "bob"));
    }

    @Test
    void acceptIncomingFromUsername_success_setsAccepted_andSaves() {
        User requester = mock(User.class);
        when(requester.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(requester));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.REQUESTED);

        User addressee = mock(User.class);
        when(addressee.getId()).thenReturn(1L);
        when(friendship.getAddressee()).thenReturn(addressee);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(friendship)).thenReturn(friendship);

        Friendship result = service.acceptIncomingFromUsername(1L, "bob");

        assertNotNull(result);
        verify(friendship).setStatus(FriendshipStatus.ACCEPTED);
        verify(friendshipRepository).save(friendship);
    }

    /* ===================== declineIncomingFromUsername ===================== */

    @Test
    void declineIncomingFromUsername_notRequested_throws() {
        User requester = mock(User.class);
        when(requester.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(requester));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.ACCEPTED);
        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(IllegalArgumentException.class,
                () -> service.declineIncomingFromUsername(1L, "bob"));
    }

    @Test
    void declineIncomingFromUsername_notIncoming_throws() {
        User requester = mock(User.class);
        when(requester.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(requester));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.REQUESTED);

        User addressee = mock(User.class);
        when(addressee.getId()).thenReturn(999L);
        when(friendship.getAddressee()).thenReturn(addressee);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(IllegalArgumentException.class,
                () -> service.declineIncomingFromUsername(1L, "bob"));
    }

    @Test
    void declineIncomingFromUsername_success_deletesFriendship() {
        User requester = mock(User.class);
        when(requester.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(requester));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.REQUESTED);

        User addressee = mock(User.class);
        when(addressee.getId()).thenReturn(1L);
        when(friendship.getAddressee()).thenReturn(addressee);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        service.declineIncomingFromUsername(1L, "bob");

        verify(friendshipRepository).delete(friendship);
    }

    /* ===================== cancelOutgoingToUsername ===================== */

    @Test
    void cancelOutgoingToUsername_notRequested_throws() {
        User addressee = mock(User.class);
        when(addressee.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(addressee));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(IllegalArgumentException.class,
                () -> service.cancelOutgoingToUsername(1L, "bob"));
    }

    @Test
    void cancelOutgoingToUsername_notOutgoing_throws() {
        User addressee = mock(User.class);
        when(addressee.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(addressee));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.REQUESTED);

        User requester = mock(User.class);
        when(requester.getId()).thenReturn(999L);
        when(friendship.getRequester()).thenReturn(requester);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        assertThrows(IllegalArgumentException.class,
                () -> service.cancelOutgoingToUsername(1L, "bob"));
    }

    @Test
    void cancelOutgoingToUsername_success_deletesFriendship() {
        User addressee = mock(User.class);
        when(addressee.getId()).thenReturn(2L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(addressee));

        Friendship friendship = mock(Friendship.class);
        when(friendship.getStatus()).thenReturn(FriendshipStatus.REQUESTED);

        User requester = mock(User.class);
        when(requester.getId()).thenReturn(1L);
        when(friendship.getRequester()).thenReturn(requester);

        when(friendshipRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(friendship));

        service.cancelOutgoingToUsername(1L, "bob");

        verify(friendshipRepository).delete(friendship);
    }

    /* ===================== Lists ===================== */

    @Test
    void getIncomingRequests_delegatesToRepo() {
        List<Friendship> list = List.of(mock(Friendship.class));
        when(friendshipRepository.findByAddressee_IdAndStatus(1L, FriendshipStatus.REQUESTED))
                .thenReturn(list);

        List<Friendship> result = service.getIncomingRequests(1L);

        assertSame(list, result);
        verify(friendshipRepository).findByAddressee_IdAndStatus(1L, FriendshipStatus.REQUESTED);
    }

    @Test
    void getOutgoingRequests_delegatesToRepo() {
        List<Friendship> list = List.of(mock(Friendship.class));
        when(friendshipRepository.findByRequester_IdAndStatus(1L, FriendshipStatus.REQUESTED))
                .thenReturn(list);

        List<Friendship> result = service.getOutgoingRequests(1L);

        assertSame(list, result);
        verify(friendshipRepository).findByRequester_IdAndStatus(1L, FriendshipStatus.REQUESTED);
    }

    @Test
    void getFriends_delegatesToRepo() {
        List<Friendship> list = List.of(mock(Friendship.class));
        when(friendshipRepository.findByStatusAndRequester_IdOrStatusAndAddressee_Id(
                FriendshipStatus.ACCEPTED, 1L,
                FriendshipStatus.ACCEPTED, 1L
        )).thenReturn(list);

        List<Friendship> result = service.getFriends(1L);

        assertSame(list, result);
        verify(friendshipRepository).findByStatusAndRequester_IdOrStatusAndAddressee_Id(
                FriendshipStatus.ACCEPTED, 1L,
                FriendshipStatus.ACCEPTED, 1L
        );
    }

    @Test
    void getFriendsOfUsername_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getFriendsOfUsername("   "));
        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void getFriendsOfUsername_userNotFound_throws() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getFriendsOfUsername("bob"));
    }

    @Test
    void getFriendsOfUsername_success_callsGetFriends() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        List<Friendship> list = List.of(mock(Friendship.class));
        when(friendshipRepository.findByStatusAndRequester_IdOrStatusAndAddressee_Id(
                FriendshipStatus.ACCEPTED, 1L,
                FriendshipStatus.ACCEPTED, 1L
        )).thenReturn(list);

        List<Friendship> result = service.getFriendsOfUsername("bob");

        assertSame(list, result);
    }
}
