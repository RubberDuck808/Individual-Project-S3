package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static nl.fontys.db3.backend.service.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AvatarService avatarService;

    @Mock
    private BackgroundService backgroundService;

    @InjectMocks
    private UserService userService;

    private Role userRole;
    private User testUser;
    private Avatar testAvatar;
    private Background testBackground;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name(ROLE_USER).build();
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .name("Test User")
                .password("encodedPassword")
                .role(userRole)
                .createdAt(LocalDateTime.now())
                .build();

        testAvatar = Avatar.builder()
                .id(1L)
                .name("test-avatar")
                .imagePath("/avatars/test.png")
                .active(true)
                .build();

        testBackground = Background.builder()
                .id(1L)
                .name("test-background")
                .imagePath("/backgrounds/test.png")
                .active(true)
                .build();
    }

    // Tests for deleteUser
    @Test
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_userNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(1L);
        });

        assertEquals("User not found with id 1", exception.getMessage());
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    // Tests for updateMe
    @Test
    void updateMe_updateName_success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateMe("test@test.com", "New Name", null, null, null, null);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        verify(userRepository).findByEmail("test@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateMe_updateUsername_success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateMe("test@test.com", null, "newusername", null, null, null);

        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
        verify(userRepository).existsByUsername("newusername");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateMe_updateUsername_alreadyExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMe("test@test.com", null, "existinguser", null, null, null);
        });

        assertEquals(USERNAME_ALREADY_EXISTS, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMe_updateEmail_success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(userRepository.existsByEmail("newemail@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateMe("test@test.com", null, null, "newemail@test.com", "currentPass", null);

        assertNotNull(result);
        assertEquals("newemail@test.com", result.getEmail());
        verify(passwordEncoder).matches("currentPass", "encodedPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateMe_updateEmail_missingPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMe("test@test.com", null, null, "newemail@test.com", null, null);
        });

        assertEquals(CURRENT_PASSWORD_REQUIRED, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMe_updateEmail_incorrectPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMe("test@test.com", null, null, "newemail@test.com", "wrongPass", null);
        });

        assertEquals(CURRENT_PASSWORD_INCORRECT, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMe_updateEmail_alreadyExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMe("test@test.com", null, null, "existing@test.com", "currentPass", null);
        });

        assertEquals(EMAIL_ALREADY_EXISTS, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMe_updatePassword_success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateMe("test@test.com", null, null, null, "currentPass", "newPassword");

        assertNotNull(result);
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateMe_updatePassword_missingCurrentPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMe("test@test.com", null, null, null, null, "newPassword");
        });

        assertEquals(CURRENT_PASSWORD_REQUIRED, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMe_updatePassword_incorrectCurrentPassword() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMe("test@test.com", null, null, null, "wrongPass", "newPassword");
        });

        assertEquals(CURRENT_PASSWORD_INCORRECT, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateMe_notAuthenticated() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMe(null, "New Name", null, null, null, null);
        });

        assertEquals(NOT_AUTHENTICATED, exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void updateMe_userNotFound() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateMe("nonexistent@test.com", "New Name", null, null, null, null);
        });

        assertEquals(USER_NOT_FOUND, exception.getMessage());
    }

    // Tests for changeMyAvatar
    @Test
    void changeMyAvatar_success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(avatarService.getActiveAvatarByNameOrThrow("test-avatar")).thenReturn(testAvatar);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.changeMyAvatar("test@test.com", "test-avatar");

        assertNotNull(result);
        assertEquals(testAvatar, result.getAvatar());
        verify(avatarService).getActiveAvatarByNameOrThrow("test-avatar");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changeMyAvatar_notAuthenticated() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changeMyAvatar(null, "test-avatar");
        });

        assertEquals(NOT_AUTHENTICATED, exception.getMessage());
        verify(avatarService, never()).getActiveAvatarByNameOrThrow(anyString());
    }

    @Test
    void changeMyAvatar_userNotFound() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changeMyAvatar("nonexistent@test.com", "test-avatar");
        });

        assertEquals(USER_NOT_FOUND, exception.getMessage());
        verify(avatarService, never()).getActiveAvatarByNameOrThrow(anyString());
    }

    // Tests for changeMyBackground
    @Test
    void changeMyBackground_success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(backgroundService.getActiveBackgroundByNameOrThrow("test-background")).thenReturn(testBackground);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.changeMyBackground("test@test.com", "test-background");

        assertNotNull(result);
        assertEquals(testBackground, result.getBackground());
        verify(backgroundService).getActiveBackgroundByNameOrThrow("test-background");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changeMyBackground_notAuthenticated() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changeMyBackground(null, "test-background");
        });

        assertEquals(NOT_AUTHENTICATED, exception.getMessage());
        verify(backgroundService, never()).getActiveBackgroundByNameOrThrow(anyString());
    }

    @Test
    void changeMyBackground_userNotFound() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.changeMyBackground("nonexistent@test.com", "test-background");
        });

        assertEquals(USER_NOT_FOUND, exception.getMessage());
        verify(backgroundService, never()).getActiveBackgroundByNameOrThrow(anyString());
    }
}
