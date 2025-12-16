package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.AuthRequest;
import nl.fontys.db3.backend.dto.AuthResponse;
import nl.fontys.db3.backend.dto.UserDTO;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.security.JwtService;
import nl.fontys.db3.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(UserDTO.fromEntity(created));
    }

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
                new AuthResponse(token, UserDTO.fromEntity(user))
        );
    }
}
