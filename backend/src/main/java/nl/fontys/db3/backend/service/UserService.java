package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.service.AvatarService;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.service.BackgroundService;




import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AvatarService avatarService;
    private final BackgroundService backgroundService;



    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            AvatarService avatarService,
            BackgroundService backgroundService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.avatarService = avatarService;
        this.backgroundService = backgroundService;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String username = user.getUsername().trim().toLowerCase();
        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // fetch default role USER
        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new IllegalStateException("Default role USER not found"));

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        // assign role automatically
        user.setRole(userRole);

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Gets a user by username (username normalized to lowercase).
     */
    public User getByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        return userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * Find by username OR email (normalizes both to lowercase + trims).
     */
    public Optional<User> findByUsernameOrEmail(String username, String email) {
        if (username != null && !username.isBlank()) {
            return userRepository.findByUsername(username.trim().toLowerCase());
        } else if (email != null && !email.isBlank()) {
            return userRepository.findByEmail(email.trim().toLowerCase());
        }
        return Optional.empty();
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /* ===================== UPDATE ME (Settings) ===================== */

    @Transactional
    public User updateMe(
            String currentEmailFromJwt,
            String name,
            String username,
            String email,
            String currentPassword,
            String newPassword
    ) {
        if (currentEmailFromJwt == null || currentEmailFromJwt.isBlank()) {
            throw new IllegalArgumentException("Not authenticated");
        }

        User user = userRepository.findByEmail(currentEmailFromJwt.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Normalize inputs
        String nextName = (name != null) ? name.trim() : null;
        String nextUsername = (username != null) ? username.trim().toLowerCase() : null;
        String nextEmail = (email != null) ? email.trim().toLowerCase() : null;

        boolean wantsUsernameChange = nextUsername != null && !nextUsername.isBlank()
                && !nextUsername.equals(user.getUsername());

        boolean wantsEmailChange = nextEmail != null && !nextEmail.isBlank()
                && !nextEmail.equals(user.getEmail());

        boolean wantsPasswordChange = newPassword != null && !newPassword.isBlank();

        // Require current password for sensitive changes
        if (wantsEmailChange || wantsPasswordChange) {
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new IllegalArgumentException("Current password is required");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
        }

        // Update name (non-sensitive)
        if (nextName != null && !nextName.isBlank()) {
            user.setName(nextName);
        }

        // Update username (unique)
        if (wantsUsernameChange) {
            if (userRepository.existsByUsername(nextUsername)) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(nextUsername);
        }

        // Update email (unique) - sensitive
        if (wantsEmailChange) {
            if (userRepository.existsByEmail(nextEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(nextEmail);
        }

        // Update password - sensitive
        if (wantsPasswordChange) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        return userRepository.save(user);
    }

    @Transactional
public User changeMyAvatar(String currentEmailFromJwt, String avatarName) {
    if (currentEmailFromJwt == null || currentEmailFromJwt.isBlank()) {
        throw new IllegalArgumentException("Not authenticated");
    }

    User user = userRepository.findByEmail(currentEmailFromJwt.trim().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Avatar avatar = avatarService.getActiveAvatarByNameOrThrow(avatarName);

    user.setAvatar(avatar);
    return userRepository.save(user);
}


@Transactional
public User changeMyBackground(String currentEmailFromJwt, String backgroundName) {
    if (currentEmailFromJwt == null || currentEmailFromJwt.isBlank()) {
        throw new IllegalArgumentException("Not authenticated");
    }

    User user = userRepository.findByEmail(currentEmailFromJwt.trim().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Background background = backgroundService.getActiveBackgroundByNameOrThrow(backgroundName);

    user.setBackground(background);
    return userRepository.save(user);
}


}
