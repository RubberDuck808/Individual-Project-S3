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
class RoleRepositoryIT {

    @Autowired
    RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        try {
            roleRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }
    }

    @Test
    void saveAndFindById() {
        Role role = Role.builder()
                .name("USER")
                .build();

        Role saved = roleRepository.save(role);
        assertNotNull(saved.getId());

        Role found = roleRepository.findById(saved.getId()).orElseThrow();
        assertEquals("USER", found.getName());
    }

    @Test
    void findByName() {
        Role role = Role.builder()
                .name("ADMIN")
                .build();
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByName("ADMIN");
        assertTrue(found.isPresent());
        assertEquals("ADMIN", found.get().getName());
    }

    @Test
    void findByName_NotFound() {
        Optional<Role> found = roleRepository.findByName("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void findAll() {
        Role role1 = Role.builder()
                .name("USER")
                .build();
        roleRepository.save(role1);

        Role role2 = Role.builder()
                .name("ADMIN")
                .build();
        roleRepository.save(role2);

        assertEquals(2, roleRepository.findAll().size());
    }
}
