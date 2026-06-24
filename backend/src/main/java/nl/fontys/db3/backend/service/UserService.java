package nl.fontys.db3.backend.service;

import lombok.extern.slf4j.Slf4j;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.entity.Background;

import nl.fontys.db3.backend.exception.NotFoundException;

import static nl.fontys.db3.backend.service.Constants.USER_NOT_FOUND;
import static nl.fontys.db3.backend.service.Constants.ROLE_USER;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AvatarService avatarService;
    private final BackgroundService backgroundService;
    private final StatisticsService statisticsService;



    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            AvatarService avatarService,
            BackgroundService backgroundService,
            StatisticsService statisticsService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.avatarService = avatarService;
        this.backgroundService = backgroundService;
        this.statisticsService = statisticsService;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        log.debug("Creating user - username: {}, email: {}", user.getUsername(), user.getEmail());
        
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            log.warn("User creation failed - username is required");
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User creation failed - email is required");
            throw new IllegalArgumentException("Email is required");
        }

        String username = user.getUsername().trim().toLowerCase();
        String email = user.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            log.warn("User creation failed - username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("User creation failed - email already exists: {}", email);
            throw new IllegalArgumentException("Email already exists");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("User creation failed - name is required");
            throw new IllegalArgumentException("Name is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            log.warn("User creation failed - password is required");
            throw new IllegalArgumentException("Password is required");
        }

        Role userRole = roleRepository.findByName(ROLE_USER)
            .orElseThrow(() -> {
                log.error("User creation failed - default role {} not found", ROLE_USER);
                return new IllegalStateException("Default role " + ROLE_USER + " not found");
            });

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(userRole);

        User saved = userRepository.save(user);
        
        // Ensure statistics row exists for the new user
        statisticsService.ensureStatsRow(saved);
        
        log.info("User created successfully - userId: {}, username: {}, email: {}", 
                saved.getId(), saved.getUsername(), saved.getEmail());
        return saved;
    }

    public void deleteUser(Long id) {
        log.info("Deleting user - userId: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User deletion failed - user not found: userId: {}", id);
            throw new NotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted successfully - userId: {}", id);
    }

    /**
     * Gets a user by username (username normalized to lowercase).
     */
    public User getByUsername(String username) {
        log.debug("Getting user by username - username: {}", username);
        if (username == null || username.isBlank()) {
            log.warn("Get user failed - username is required");
            throw new IllegalArgumentException("Username is required");
        }
        return userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("Get user failed - user not found: username: {}", username);
                    return new NotFoundException(USER_NOT_FOUND);
                });
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

    @Transactional
    public User updateMe(
            String currentEmailFromJwt,
            String name,
            String username,
            String email,
            String currentPassword,
            String newPassword
    ) {
        log.debug("Updating user - email: {}, fields: name={}, username={}, email={}, passwordChange={}", 
                currentEmailFromJwt, name != null, username != null, email != null, newPassword != null);
        
        if (currentEmailFromJwt == null || currentEmailFromJwt.isBlank()) {
            log.warn("User update failed - not authenticated");
            throw new IllegalArgumentException(Constants.NOT_AUTHENTICATED);
        }

        User user = userRepository.findByEmail(currentEmailFromJwt.trim().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("User update failed - user not found: email: {}", currentEmailFromJwt);
                    return new IllegalArgumentException(USER_NOT_FOUND);
                });

        String nextName = (name != null) ? name.trim() : null;
        String nextUsername = (username != null) ? username.trim().toLowerCase() : null;
        String nextEmail = (email != null) ? email.trim().toLowerCase() : null;

        try {
            validateAndApplyNameChange(user, nextName);
            validateAndApplyUsernameChange(user, nextUsername);
            validateAndApplyEmailChange(user, nextEmail, currentPassword);
            validateAndApplyPasswordChange(user, newPassword, currentPassword);

            User saved = userRepository.save(user);
            log.info("User updated successfully - userId: {}, username: {}", saved.getId(), saved.getUsername());
            return saved;
        } catch (IllegalArgumentException e) {
            log.warn("User update failed - userId: {}, reason: {}", user.getId(), e.getMessage());
            throw e;
        }
    }
    
    private void validateAndApplyNameChange(User user, String nextName) {
        if (nextName != null && !nextName.isBlank()) {
            user.setName(nextName);
        }
    }
    
    private void validateAndApplyUsernameChange(User user, String nextUsername) {
        if (nextUsername == null || nextUsername.isBlank() || nextUsername.equals(user.getUsername())) {
            return;
        }
        
        if (userRepository.existsByUsername(nextUsername)) {
            log.warn("Username change failed - username already exists: userId: {}, newUsername: {}", 
                    user.getId(), nextUsername);
            throw new IllegalArgumentException(Constants.USERNAME_ALREADY_EXISTS);
        }
        log.debug("Username changed - userId: {}, oldUsername: {}, newUsername: {}", 
                user.getId(), user.getUsername(), nextUsername);
        user.setUsername(nextUsername);
    }
    
    private void validateAndApplyEmailChange(User user, String nextEmail, String currentPassword) {
        if (nextEmail == null || nextEmail.isBlank() || nextEmail.equals(user.getEmail())) {
            return;
        }
        
        if (currentPassword == null || currentPassword.isBlank()) {
            log.warn("Email change failed - current password required: userId: {}", user.getId());
            throw new IllegalArgumentException(Constants.CURRENT_PASSWORD_REQUIRED);
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Email change failed - incorrect current password: userId: {}", user.getId());
            throw new IllegalArgumentException(Constants.CURRENT_PASSWORD_INCORRECT);
        }
        
        if (userRepository.existsByEmail(nextEmail)) {
            log.warn("Email change failed - email already exists: userId: {}, newEmail: {}", 
                    user.getId(), nextEmail);
            throw new IllegalArgumentException(Constants.EMAIL_ALREADY_EXISTS);
        }
        log.debug("Email changed - userId: {}, oldEmail: {}, newEmail: {}", 
                user.getId(), user.getEmail(), nextEmail);
        user.setEmail(nextEmail);
    }
    
    private void validateAndApplyPasswordChange(User user, String newPassword, String currentPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return;
        }
        
        if (currentPassword == null || currentPassword.isBlank()) {
            log.warn("Password change failed - current password required: userId: {}", user.getId());
            throw new IllegalArgumentException(Constants.CURRENT_PASSWORD_REQUIRED);
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Password change failed - incorrect current password: userId: {}", user.getId());
            throw new IllegalArgumentException(Constants.CURRENT_PASSWORD_INCORRECT);
        }
        
        log.debug("Password changed - userId: {}", user.getId());
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public User changeMyAvatar(String currentEmailFromJwt, String avatarName) {
        log.debug("Changing avatar - email: {}, avatarName: {}", currentEmailFromJwt, avatarName);
        if (currentEmailFromJwt == null || currentEmailFromJwt.isBlank()) {
            log.warn("Avatar change failed - not authenticated");
            throw new IllegalArgumentException(Constants.NOT_AUTHENTICATED);
        }

        User user = userRepository.findByEmail(currentEmailFromJwt.trim().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("Avatar change failed - user not found: email: {}", currentEmailFromJwt);
                    return new IllegalArgumentException(USER_NOT_FOUND);
                });

        Avatar avatar = avatarService.getActiveAvatarByNameOrThrow(avatarName);
        user.setAvatar(avatar);
        User saved = userRepository.save(user);
        log.info("Avatar changed successfully - userId: {}, avatarName: {}", saved.getId(), avatarName);
        return saved;
    }

    @Transactional
    public User changeMyBackground(String currentEmailFromJwt, String backgroundName) {
        log.debug("Changing background - email: {}, backgroundName: {}", currentEmailFromJwt, backgroundName);
        if (currentEmailFromJwt == null || currentEmailFromJwt.isBlank()) {
            log.warn("Background change failed - not authenticated");
            throw new IllegalArgumentException(Constants.NOT_AUTHENTICATED);
        }

        User user = userRepository.findByEmail(currentEmailFromJwt.trim().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("Background change failed - user not found: email: {}", currentEmailFromJwt);
                    return new IllegalArgumentException(USER_NOT_FOUND);
                });

        Background background = backgroundService.getActiveBackgroundByNameOrThrow(backgroundName);
        user.setBackground(background);
        User saved = userRepository.save(user);
        log.info("Background changed successfully - userId: {}, backgroundName: {}", saved.getId(), backgroundName);
        return saved;
    }


}
