package nl.fontys.db3.backend.controller;

import lombok.extern.slf4j.Slf4j;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import nl.fontys.db3.backend.dto.ChangeAvatarRequest;
import nl.fontys.db3.backend.dto.ChangeBackgroundRequest;

import java.util.Map;

@Slf4j
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

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest req) {
        log.info("User registration attempt - username: {}, email: {}", req.username(), req.email());
        try {
            User created = userService.createUser(
                    User.builder()
                            .username(req.username())
                            .email(req.email())
                            .password(req.password())
                            .name(req.name())
                            .build()
            );
            log.info("User registered successfully - userId: {}, username: {}, email: {}", 
                    created.getId(), created.getUsername(), created.getEmail());
            return ResponseEntity.status(201).body(userMapper.toUserDTO(created));
        } catch (IllegalArgumentException e) {
            log.warn("User registration failed - username: {}, email: {}, reason: {}", 
                    req.username(), req.email(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user registration - username: {}, email: {}", 
                    req.username(), req.email(), e);
            throw e;
        }
    }

    public record RegisterRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 30, message = "Username must be 3–30 characters")
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            @NotBlank(message = "Name is required")
            @Size(max = 100, message = "Name must be at most 100 characters")
            String name
    ) {}

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.debug("Login attempt - email: {}", request.getEmail());
        try {
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

            log.info("User logged in successfully - userId: {}, username: {}, email: {}", 
                    user.getId(), user.getUsername(), user.getEmail());
            return ResponseEntity.ok(
                    new AuthResponse(token, userMapper.toUserDTO(user))
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Login failed - email: {}, reason: {}", request.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during login - email: {}", request.getEmail(), e);
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            log.warn("Get current user failed - no authentication");
            return ResponseEntity.status(401).build();
        }

        log.debug("Get current user request - username: {}", userDetails.getUsername());
        try {
            User user = userService
                    .findByUsernameOrEmail(null, userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(userMapper.toUserDTO(user));
        } catch (Exception e) {
            log.error("Error getting current user - username: {}", userDetails.getUsername(), e);
            throw e;
        }
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUser req
    ) {
        if (userDetails == null) {
            log.warn("Update user failed - no authentication");
            return ResponseEntity.status(401).build();
        }

        log.info("User update request - username: {}, fields: name={}, username={}, email={}, passwordChange={}", 
                userDetails.getUsername(), req.getName() != null, req.getUsername() != null, 
                req.getEmail() != null, req.getNewPassword() != null);
        try {
            User updated = userService.updateMe(
                    userDetails.getUsername(),
                    req.getName(),
                    req.getUsername(),
                    req.getEmail(),
                    req.getCurrentPassword(),
                    req.getNewPassword()
            );

            log.info("User updated successfully - userId: {}, username: {}", 
                    updated.getId(), updated.getUsername());
            return ResponseEntity.ok(userMapper.toUserDTO(updated));
        } catch (IllegalArgumentException e) {
            log.warn("User update failed - username: {}, reason: {}", 
                    userDetails.getUsername(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user update - username: {}", 
                    userDetails.getUsername(), e);
            throw e;
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicUserDTO> getByUsername(@PathVariable String username) {
        log.debug("Get user by username request - username: {}", username);
        try {
            User user = userService.getByUsername(username);
            return ResponseEntity.ok(userMapper.toPublicUserDTO(user));
        } catch (IllegalArgumentException e) {
            log.warn("Get user by username failed - username: {}, reason: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error getting user by username - username: {}", username, e);
            throw e;
        }
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<UserDTO> changeMyAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangeAvatarRequest req
    ) {
        if (userDetails == null) {
            log.warn("Change avatar failed - no authentication");
            return ResponseEntity.status(401).build();
        }

        log.info("Avatar change request - username: {}, avatarName: {}", 
                userDetails.getUsername(), req.avatarName());
        try {
            User updated = userService.changeMyAvatar(userDetails.getUsername(), req.avatarName());
            log.info("Avatar changed successfully - userId: {}, avatarName: {}", 
                    updated.getId(), req.avatarName());
            return ResponseEntity.ok(userMapper.toUserDTO(updated));
        } catch (Exception e) {
            log.error("Error changing avatar - username: {}, avatarName: {}", 
                    userDetails.getUsername(), req.avatarName(), e);
            throw e;
        }
    }

    @PutMapping("/me/background")
    public ResponseEntity<UserDTO> changeMyBackground(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangeBackgroundRequest req
    ) {
        if (userDetails == null) {
            log.warn("Change background failed - no authentication");
            return ResponseEntity.status(401).build();
        }

        log.info("Background change request - username: {}, backgroundName: {}", 
                userDetails.getUsername(), req.backgroundName());
        try {
            User updated = userService.changeMyBackground(userDetails.getUsername(), req.backgroundName());
            log.info("Background changed successfully - userId: {}, backgroundName: {}", 
                    updated.getId(), req.backgroundName());
            return ResponseEntity.ok(userMapper.toUserDTO(updated));
        } catch (Exception e) {
            log.error("Error changing background - username: {}, backgroundName: {}", 
                    userDetails.getUsername(), req.backgroundName(), e);
            throw e;
        }
    }




}
