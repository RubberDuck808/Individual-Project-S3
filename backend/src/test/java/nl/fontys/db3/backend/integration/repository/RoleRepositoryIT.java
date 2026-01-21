package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class RoleRepositoryIT {

    @Autowired
    RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Migration V4__Seed_roles.sql already creates USER and ADMIN roles
        // Tests should work with existing roles, not delete them
    }

    @Test
    void saveAndFindById() {
        // Use existing USER role from migration, or create a test role with unique name
        Role role = Role.builder()
                .name("TEST_ROLE")
                .build();

        Role saved = roleRepository.save(role);
        assertNotNull(saved.getId());

        Role found = roleRepository.findById(saved.getId()).orElseThrow();
        assertEquals("TEST_ROLE", found.getName());
    }

    @Test
    void findByName() {
        // Use existing ADMIN role from migration
        Optional<Role> found = roleRepository.findByName("ADMIN");
        assertTrue(found.isPresent(), "ADMIN role should exist from migration");
        assertEquals("ADMIN", found.get().getName());
    }

    @Test
    void findByName_NotFound() {
        Optional<Role> found = roleRepository.findByName("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void findAll() {
        // Migration creates USER and ADMIN, so we should have at least 2 roles
        long count = roleRepository.findAll().size();
        assertTrue(count >= 2, "Should have at least USER and ADMIN roles from migration");
        
        // Verify both roles exist
        assertTrue(roleRepository.findByName("USER").isPresent());
        assertTrue(roleRepository.findByName("ADMIN").isPresent());
    }
}
