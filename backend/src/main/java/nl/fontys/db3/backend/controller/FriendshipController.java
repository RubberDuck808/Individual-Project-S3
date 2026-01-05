package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.FriendshipDTO;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.mapper.FriendshipMapper;
import nl.fontys.db3.backend.service.FriendshipService;
import nl.fontys.db3.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;
    private final FriendshipMapper friendshipMapper;

    public FriendshipController(FriendshipService friendshipService,
                                UserService userService,
                                FriendshipMapper friendshipMapper) {
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.friendshipMapper = friendshipMapper;
    }

    private User currentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized");
        }
        return userService
                .findByUsernameOrEmail(null, userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // --- Requests ---
    
    /** Send friend request by username */
    @PostMapping("/request")
    public ResponseEntity<FriendshipDTO> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody SendFriendRequestRequest body
    ) {
        User me = currentUser(userDetails);
        return ResponseEntity.ok(
                friendshipMapper.toDTO(
                        friendshipService.sendRequestByUsername(me.getId(), body.username())
                )
        );
    }

    public record SendFriendRequestRequest(String username) {}

    /** Accept incoming request from username */
    @PostMapping("/accept/{username}")
    public ResponseEntity<FriendshipDTO> accept(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username
    ) {
        User me = currentUser(userDetails);
        return ResponseEntity.ok(
                friendshipMapper.toDTO(
                        friendshipService.acceptIncomingFromUsername(me.getId(), username)
                )
        );
    }

    /** Decline incoming request from username */
    @DeleteMapping("/decline/{username}")
    public ResponseEntity<Void> decline(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username
    ) {
        User me = currentUser(userDetails);
        friendshipService.declineIncomingFromUsername(me.getId(), username);
        return ResponseEntity.noContent().build();
    }

    /** Cancel outgoing request to username */
    @DeleteMapping("/cancel/{username}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username
    ) {
        User me = currentUser(userDetails);
        friendshipService.cancelOutgoingToUsername(me.getId(), username);
        return ResponseEntity.noContent().build();
    }

    /** Unfriend by username */
    @DeleteMapping("/unfriend/{username}")
    public ResponseEntity<Void> unfriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String username
    ) {
        User me = currentUser(userDetails);
        friendshipService.unfriendByUsername(me.getId(), username);
        return ResponseEntity.noContent().build();
    }

    // --- Lists ---

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendshipDTO>> incoming(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User me = currentUser(userDetails);
        return ResponseEntity.ok(
                friendshipMapper.toDTOList(
                        friendshipService.getIncomingRequests(me.getId())
                )
        );
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendshipDTO>> outgoing(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User me = currentUser(userDetails);
        return ResponseEntity.ok(
                friendshipMapper.toDTOList(
                        friendshipService.getOutgoingRequests(me.getId())
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<FriendshipDTO>> friends(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User me = currentUser(userDetails);
        return ResponseEntity.ok(
                friendshipMapper.toDTOList(
                        friendshipService.getFriends(me.getId())
                )
        );
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<FriendshipDTO>> friendsOfUser(
                @PathVariable String username
    ) {
        return ResponseEntity.ok(
                friendshipMapper.toDTOList(
                        friendshipService.getFriendsOfUsername(username)
                )
        );
    }
}
