package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Friendship;
import nl.fontys.db3.backend.entity.FriendshipStatus;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.exception.NotFoundException;
import nl.fontys.db3.backend.repository.FriendshipRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static nl.fontys.db3.backend.service.Constants.FRIEND_REQUEST_NOT_FOUND;
import static nl.fontys.db3.backend.service.Constants.USER_NOT_FOUND;

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
                .orElseThrow(() -> new NotFoundException("Requester not found"));

        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

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
        unfriendInternal(userId, otherUserId);
    }

    @Transactional
    public Friendship sendRequestByUsername(Long requesterId, String addresseeUsername) {
        String username = normalizeUsername(addresseeUsername);

        User addressee = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        // Inline the sendRequest logic to avoid transactional method call via 'this'
        return sendRequestInternal(requesterId, addressee.getId());
    }

    @Transactional
    public void unfriendByUsername(Long userId, String otherUsername) {
        String username = normalizeUsername(otherUsername);

        User other = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        // Inline the unfriend logic to avoid transactional method call via 'this'
        unfriendInternal(userId, other.getId());
    }
    
    // Internal methods that extract common logic to avoid duplication
    private void validateNotSelf(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("You cannot friend yourself");
        }
    }

    private void validateNoExistingFriendship(Long requesterId, Long addresseeId) {
        friendshipRepository.findBetweenUsers(requesterId, addresseeId)
                .ifPresent(existing -> {
                    if (existing.getStatus() == FriendshipStatus.ACCEPTED) {
                        throw new IllegalArgumentException("You are already friends");
                    }
                    if (existing.getStatus() == FriendshipStatus.REQUESTED) {
                        throw new IllegalArgumentException("Friend request already exists");
                    }
                });
    }

    private User findUserById(Long userId, String errorMessage) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(errorMessage));
    }

    private Friendship createFriendshipRequest(User requester, User addressee) {
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

    private Friendship sendRequestInternal(Long requesterId, Long addresseeId) {
        validateNotSelf(requesterId, addresseeId);
        User requester = findUserById(requesterId, "Requester not found");
        User addressee = findUserById(addresseeId, USER_NOT_FOUND);
        validateNoExistingFriendship(requesterId, addresseeId);
        return createFriendshipRequest(requester, addressee);
    }
    
    private void validateFriendshipStatus(Friendship friendship) {
        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new IllegalArgumentException("You are not friends");
        }
    }

    private void unfriendInternal(Long userId, Long otherUserId) {
        Friendship friendship = friendshipRepository
                .findBetweenUsers(userId, otherUserId)
                .orElseThrow(() -> new NotFoundException("Friendship not found"));
        validateFriendshipStatus(friendship);
        friendshipRepository.delete(friendship);
    }

    @Transactional
    public Friendship acceptIncomingFromUsername(Long meId, String requesterUsername) {
        String username = normalizeUsername(requesterUsername);

        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        Friendship friendship = friendshipRepository.findBetweenUsers(meId, requester.getId())
                .orElseThrow(() -> new NotFoundException(FRIEND_REQUEST_NOT_FOUND));

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
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        Friendship friendship = friendshipRepository.findBetweenUsers(meId, requester.getId())
                .orElseThrow(() -> new NotFoundException(FRIEND_REQUEST_NOT_FOUND));

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
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        Friendship friendship = friendshipRepository.findBetweenUsers(meId, addressee.getId())
                .orElseThrow(() -> new NotFoundException(FRIEND_REQUEST_NOT_FOUND));

        if (friendship.getStatus() != FriendshipStatus.REQUESTED) {
            throw new IllegalArgumentException("Only pending requests can be cancelled");
        }

        if (!friendship.getRequester().getId().equals(meId)) {
            throw new IllegalArgumentException("No outgoing request to this user");
        }

        friendshipRepository.delete(friendship);
    }


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


    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        return username.trim().toLowerCase();
    }

    public List<Friendship> getFriendsOfUsername(String username) {
        String u = normalizeUsername(username);

        User user = userRepository.findByUsername(u)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        return getFriends(user.getId());
    }

}
