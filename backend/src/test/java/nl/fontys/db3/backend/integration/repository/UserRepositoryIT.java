package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true"
})
class UserRepositoryIT {

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;

    private Role userRole;

    @BeforeEach
    void setUp() {
        try {
            userRepository.deleteAll();
            roleRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet - schema will be created on first save
        }
        
        userRole = Role.builder().name("USER").build();
        roleRepository.save(userRole);
    }

    @Test
    void saveAndFindById() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .password("encoded")
                .role(userRole)
                .build();

        User saved = userRepository.save(user);
        assertNotNull(saved.getId());

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByUsername() {
        User user = User.builder()
                .username("alice")
                .email("alice@example.com")
                .name("Alice")
                .password("encoded")
                .role(userRole)
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("alice");
        assertTrue(found.isPresent());
        assertEquals("alice", found.get().getUsername());
    }

    @Test
    void findByEmail() {
        User user = User.builder()
                .username("bob")
                .email("bob@example.com")
                .name("Bob")
                .password("encoded")
                .role(userRole)
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("bob@example.com");
        assertTrue(found.isPresent());
        assertEquals("bob@example.com", found.get().getEmail());
    }

    @Test
    void existsByUsername() {
        User user = User.builder()
                .username("charlie")
                .email("charlie@example.com")
                .name("Charlie")
                .password("encoded")
                .role(userRole)
                .build();
        userRepository.save(user);

        assertTrue(userRepository.existsByUsername("charlie"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void existsByEmail() {
        User user = User.builder()
                .username("dave")
                .email("dave@example.com")
                .name("Dave")
                .password("encoded")
                .role(userRole)
                .build();
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("dave@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }
}
