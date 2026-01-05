package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
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

    @InjectMocks
    private UserService userService;

    /* ===================== createUser ===================== */

    @Test
    void createUser_success_assignsDefaultRoleAndEncodesPassword() {
        // Arrange
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

        // Act
        User result = userService.createUser(input);

        // Assert
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
    void createUser_missingEmail_throws() {
        User u = new User();
        u.setUsername("x");
        u.setName("A");
        u.setPassword("p");

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(u));
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
    void createUser_emailAlreadyExists_throws() {
        User u = new User();
        u.setUsername("John");
        u.setEmail("john@example.com");
        u.setName("John");
        u.setPassword("p");

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

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

    /* ===================== deleteUser ===================== */

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(5L)).thenReturn(true);

        userService.deleteUser(5L);

        verify(userRepository).deleteById(5L);
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.existsById(5L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(5L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    /* ===================== getByUsername ===================== */

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
    void getByUsername_notFound_throws() {
        when(userRepository.findByUsername("nope")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.getByUsername("nope"));
    }

    /* ===================== findByUsernameOrEmail ===================== */

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
    void findByUsernameOrEmail_usesEmailIfUsernameBlank() {
        User found = new User();
        when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.of(found));

        Optional<User> result = userService.findByUsernameOrEmail("   ", " X@Y.COM ");

        assertTrue(result.isPresent());
        verify(userRepository).findByEmail("x@y.com");
    }

    @Test
    void findByUsernameOrEmail_returnsEmptyIfBothMissing() {
        Optional<User> result = userService.findByUsernameOrEmail(null, "  ");

        assertTrue(result.isEmpty());
        verifyNoInteractions(userRepository);
    }

    /* ===================== checkPassword ===================== */

    @Test
    void checkPassword_delegatesToPasswordEncoder() {
        when(passwordEncoder.matches("raw", "enc")).thenReturn(true);

        assertTrue(userService.checkPassword("raw", "enc"));
        verify(passwordEncoder).matches("raw", "enc");
    }

    /* ===================== updateMe ===================== */

    @Test
    void updateMe_updatesNameAndUsername_withoutRequiringCurrentPassword() {
        // Arrange
        User user = new User();
        user.setEmail("me@example.com");
        user.setUsername("olduser");
        user.setName("Old Name");
        user.setPassword("ENC"); // stored encoded

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        User result = userService.updateMe(
                "ME@EXAMPLE.COM",
                " New Name ",
                " NewUser ",
                null,
                null,
                null
        );

        // Assert
        assertEquals("New Name", result.getName());
        assertEquals("newuser", result.getUsername());
        assertEquals("me@example.com", result.getEmail()); // unchanged
        verify(userRepository).save(user);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void updateMe_emailChange_requiresCurrentPassword_andUpdatesEmail() {
        // Arrange
        User user = new User();
        user.setEmail("me@example.com");
        user.setUsername("user");
        user.setPassword("ENC");

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current123", "ENC")).thenReturn(true);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        User result = userService.updateMe(
                "me@example.com",
                null,
                null,
                " NEW@MAIL.COM ",
                "current123",
                null
        );

        // Assert
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
    void updateMe_emailChange_toExistingEmail_throws() {
        User user = new User();
        user.setEmail("me@example.com");
        user.setPassword("ENC");

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current123", "ENC")).thenReturn(true);
        when(userRepository.existsByEmail("taken@mail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateMe(
                "me@example.com",
                null, null,
                "TAKEN@MAIL.COM",
                "current123",
                null
        ));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateMe_notAuthenticated_throws() {
        assertThrows(IllegalArgumentException.class, () -> userService.updateMe(
                "   ",
                null, null, null,
                null, null
        ));
        verifyNoInteractions(userRepository);
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

    /* Optional: verify what exactly got saved (nice for normalization) */
    @Test
    void createUser_savesNormalizedValues_usingCaptor() {
        User input = new User();
        input.setUsername("  A  ");
        input.setEmail("  A@B.COM  ");
        input.setName("Name");
        input.setPassword("pw");

        Role userRole = new Role();
        userRole.setName("USER");

        when(userRepository.existsByUsername("a")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("pw")).thenReturn("ENC");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(input);

        User saved = captor.getValue();
        assertEquals("a", saved.getUsername());
        assertEquals("a@b.com", saved.getEmail());
        assertEquals("ENC", saved.getPassword());
        assertNotNull(saved.getCreatedAt());
        assertEquals(userRole, saved.getRole());
        assertSame(result, saved);
    }
}
