package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.AuthRequest;
import nl.fontys.db3.backend.dto.AuthResponse;
import nl.fontys.db3.backend.dto.PublicUserDTO;
import nl.fontys.db3.backend.dto.UpdateUser;
import nl.fontys.db3.backend.dto.UserDTO;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.mapper.UserMapper;
import nl.fontys.db3.backend.security.JwtService;
import nl.fontys.db3.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import nl.fontys.db3.backend.dto.ChangeAvatarRequest;
import nl.fontys.db3.backend.dto.ChangeBackgroundRequest;



import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserMapper userMapper
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    /* Register */

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest req) {
        User created = userService.createUser(
                User.builder()
                        .username(req.username())
                        .email(req.email())
                        .password(req.password())
                        .name(req.name())
                        .build()
        );
        return ResponseEntity.ok(userMapper.toUserDTO(created));
    }

    public record RegisterRequest(String username, String email, String password, String name) {}

    /* Login */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(userDetails.getUsername(), Map.of());
        User user = userService.findByUsernameOrEmail(null, request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(
                new AuthResponse(token, userMapper.toUserDTO(user))
        );
    }

    /* Current User */

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService
                .findByUsernameOrEmail(null, userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(userMapper.toUserDTO(user));
    }

    /* Update User */

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUser req
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        User updated = userService.updateMe(
                userDetails.getUsername(),
                req.getName(),
                req.getUsername(),
                req.getEmail(),
                req.getCurrentPassword(),
                req.getNewPassword()
        );

        return ResponseEntity.ok(userMapper.toUserDTO(updated));
    }

    /* Public Profile */

    @GetMapping("/{username}")
    public ResponseEntity<PublicUserDTO> getByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        return ResponseEntity.ok(userMapper.toPublicUserDTO(user));
    }

    @PutMapping("/me/avatar")
public ResponseEntity<UserDTO> changeMyAvatar(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangeAvatarRequest req
) {
    if (userDetails == null) {
        return ResponseEntity.status(401).build();
    }

    User updated = userService.changeMyAvatar(userDetails.getUsername(), req.avatarName());
    return ResponseEntity.ok(userMapper.toUserDTO(updated));
}

@PutMapping("/me/background")
public ResponseEntity<UserDTO> changeMyBackground(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ChangeBackgroundRequest req
) {
    if (userDetails == null) {
        return ResponseEntity.status(401).build();
    }

    User updated = userService.changeMyBackground(userDetails.getUsername(), req.backgroundName());
    return ResponseEntity.ok(userMapper.toUserDTO(updated));
}




}
