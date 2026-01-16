package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private AvatarService avatarService;

    @Mock
    private BackgroundService backgroundService;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success_assignsDefaultRoleAndEncodesPassword() {
        User input = new User();
        input.setUsername("  JohnDoe  ");
        input.setEmail("  JOHN@EXAMPLE.COM ");
        input.setName("John");
        input.setPassword("plain123");

        Role userRole = new Role();
        userRole.setName("USER");

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plain123")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(input);

        assertNotNull(result);
        assertEquals("johndoe", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John", result.getName());
        assertEquals("ENCODED", result.getPassword());
        assertNotNull(result.getCreatedAt());
        assertEquals(userRole, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_missingUsername_throws() {
        User u = new User();
        u.setEmail("a@b.com");
        u.setName("A");
        u.setPassword("p");

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(u));
        verifyNoInteractions(roleRepository);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_usernameAlreadyExists_throws() {
        User u = new User();
        u.setUsername("John");
        u.setEmail("john@example.com");
        u.setName("John");
        u.setPassword("p");

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(u));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_defaultRoleMissing_throws() {
        User u = new User();
        u.setUsername("John");
        u.setEmail("john@example.com");
        u.setName("John");
        u.setPassword("p");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.createUser(u));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.existsById(5L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(5L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getByUsername_normalizesToLowercase() {
        User found = new User();
        found.setUsername("jane");

        when(userRepository.findByUsername("jane")).thenReturn(Optional.of(found));

        User result = userService.getByUsername("  JANE  ");

        assertNotNull(result);
        assertEquals("jane", result.getUsername());
        verify(userRepository).findByUsername("jane");
    }

    @Test
    void findByUsernameOrEmail_prefersUsernameWhenPresent() {
        User found = new User();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(found));

        Optional<User> result = userService.findByUsernameOrEmail(" U ", "x@y.com");

        assertTrue(result.isPresent());
        verify(userRepository).findByUsername("u");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void updateMe_updatesNameAndUsername_withoutRequiringCurrentPassword() {
        User user = new User();
        user.setEmail("me@example.com");
        user.setUsername("olduser");
        user.setName("Old Name");
        user.setPassword("ENC");

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateMe(
                "ME@EXAMPLE.COM",
                " New Name ",
                " NewUser ",
                null,
                null,
                null
        );

        assertEquals("New Name", result.getName());
        assertEquals("newuser", result.getUsername());
        assertEquals("me@example.com", result.getEmail());
        verify(userRepository).save(user);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void updateMe_emailChange_requiresCurrentPassword_andUpdatesEmail() {
        User user = new User();
        user.setEmail("me@example.com");
        user.setUsername("user");
        user.setPassword("ENC");

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current123", "ENC")).thenReturn(true);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateMe(
                "me@example.com",
                null,
                null,
                " NEW@MAIL.COM ",
                "current123",
                null
        );

        assertEquals("new@mail.com", result.getEmail());
        verify(passwordEncoder).matches("current123", "ENC");
        verify(userRepository).save(user);
    }

    @Test
    void updateMe_emailChange_wrongCurrentPassword_throws() {
        User user = new User();
        user.setEmail("me@example.com");
        user.setPassword("ENC");

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "ENC")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.updateMe(
                "me@example.com",
                null, null,
                "new@mail.com",
                "bad",
                null
        ));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMe_passwordChange_requiresCurrentPassword_andEncodesNewPassword() {
        User user = new User();
        user.setEmail("me@example.com");
        user.setPassword("ENC");

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current123", "ENC")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("NEW_ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateMe(
                "me@example.com",
                null, null, null,
                "current123",
                "newpass"
        );

        assertEquals("NEW_ENC", result.getPassword());
        verify(passwordEncoder).encode("newpass");
        verify(userRepository).save(user);
    }

    @Test
    void updateMe_usernameChange_toExistingUsername_throws() {
        User user = new User();
        user.setEmail("me@example.com");
        user.setUsername("olduser");
        user.setPassword("ENC");

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateMe(
                "me@example.com",
                null,
                "TAKEN",
                null,
                null,
                null
        ));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMe_userNotFound_throws() {
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.updateMe(
                "me@example.com",
                null, null, null,
                null, null
        ));
    }

    @Test
    void changeMyAvatar_success() {
        User user = new User();
        user.setEmail("test@test.com");
        Avatar avatar = Avatar.builder().id(1L).name("avatar1").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(avatarService.getActiveAvatarByNameOrThrow("avatar1")).thenReturn(avatar);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeMyAvatar("test@test.com", "avatar1");

        assertNotNull(result);
        assertEquals(avatar, result.getAvatar());
        verify(userRepository).save(user);
    }

    @Test
    void changeMyAvatar_userNotFound_throws() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.changeMyAvatar("test@test.com", "avatar1"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeMyBackground_success() {
        User user = new User();
        user.setEmail("test@test.com");
        Background background = Background.builder().id(1L).name("bg1").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(backgroundService.getActiveBackgroundByNameOrThrow("bg1")).thenReturn(background);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeMyBackground("test@test.com", "bg1");

        assertNotNull(result);
        assertEquals(background, result.getBackground());
        verify(userRepository).save(user);
    }

    @Test
    void changeMyBackground_userNotFound_throws() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> userService.changeMyBackground("test@test.com", "bg1"));
        verify(userRepository, never()).save(any());
    }
}