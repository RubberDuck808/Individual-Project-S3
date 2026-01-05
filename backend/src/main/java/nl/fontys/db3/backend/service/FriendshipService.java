package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Friendship;
import nl.fontys.db3.backend.entity.FriendshipStatus;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.FriendshipRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Friendship sendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("You cannot friend yourself");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        friendshipRepository.findBetweenUsers(requesterId, addresseeId)
                .ifPresent(existing -> {
                    if (existing.getStatus() == FriendshipStatus.ACCEPTED) {
                        throw new IllegalArgumentException("You are already friends");
                    }
                    if (existing.getStatus() == FriendshipStatus.REQUESTED) {
                        throw new IllegalArgumentException("Friend request already exists");
                    }
                });

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        try {
            return friendshipRepository.save(friendship);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Friend request already exists");
        }
    }

    @Transactional
    public void unfriend(Long userId, Long otherUserId) {
        Friendship friendship = friendshipRepository
                .findBetweenUsers(userId, otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalArgumentException("You are not friends");
        }

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public Friendship sendRequestByUsername(Long requesterId, String addresseeUsername) {
        String username = normalizeUsername(addresseeUsername);

        User addressee = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return sendRequest(requesterId, addressee.getId());
    }

    @Transactional
    public void unfriendByUsername(Long userId, String otherUsername) {
        String username = normalizeUsername(otherUsername);

        User other = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        unfriend(userId, other.getId());
    }

    @Transactional
    public Friendship acceptIncomingFromUsername(Long meId, String requesterUsername) {
        String username = normalizeUsername(requesterUsername);

        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Friendship friendship = friendshipRepository.findBetweenUsers(meId, requester.getId())
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (friendship.getStatus() != FriendshipStatus.REQUESTED) {
            throw new IllegalArgumentException("Only pending requests can be accepted");
        }

        if (!friendship.getAddressee().getId().equals(meId)) {
            throw new IllegalArgumentException("No incoming request from this user");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    @Transactional
    public void declineIncomingFromUsername(Long meId, String requesterUsername) {
        String username = normalizeUsername(requesterUsername);

        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Friendship friendship = friendshipRepository.findBetweenUsers(meId, requester.getId())
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (friendship.getStatus() != FriendshipStatus.REQUESTED) {
            throw new IllegalArgumentException("Only pending requests can be declined");
        }

        if (!friendship.getAddressee().getId().equals(meId)) {
            throw new IllegalArgumentException("No incoming request from this user");
        }

        friendshipRepository.delete(friendship);
    }

    @Transactional
    public void cancelOutgoingToUsername(Long meId, String addresseeUsername) {
        String username = normalizeUsername(addresseeUsername);

        User addressee = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Friendship friendship = friendshipRepository.findBetweenUsers(meId, addressee.getId())
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (friendship.getStatus() != FriendshipStatus.REQUESTED) {
            throw new IllegalArgumentException("Only pending requests can be cancelled");
        }

        if (!friendship.getRequester().getId().equals(meId)) {
            throw new IllegalArgumentException("No outgoing request to this user");
        }

        friendshipRepository.delete(friendship);
    }

    // ---------- Lists ----------

    public List<Friendship> getIncomingRequests(Long userId) {
        return friendshipRepository
                .findByAddressee_IdAndStatus(userId, FriendshipStatus.REQUESTED);
    }

    public List<Friendship> getOutgoingRequests(Long userId) {
        return friendshipRepository
                .findByRequester_IdAndStatus(userId, FriendshipStatus.REQUESTED);
    }

    public List<Friendship> getFriends(Long userId) {
        return friendshipRepository
                .findByStatusAndRequester_IdOrStatusAndAddressee_Id(
                        FriendshipStatus.ACCEPTED, userId,
                        FriendshipStatus.ACCEPTED, userId
                );
    }

    // ---------- Helpers ----------

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        return username.trim();
    }

    public List<Friendship> getFriendsOfUsername(String username) {
    String u = normalizeUsername(username);

    User user = userRepository.findByUsername(u)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    return getFriends(user.getId());
    }

}
