package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
class UserRepositoryPostgresIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;

    @Test
    void saveAndFindByEmail_works() {
        // Arrange: create required role
        Role userRole = roleRepository.save(Role.builder().name("USER").build());

        User u = User.builder()
                .email("user@test.com")
                .username("user")
                .name("User")
                .password("encoded")
                .role(userRole)
                .build();

        userRepository.save(u);

        // Act + Assert
        assertTrue(userRepository.findByEmail("user@test.com").isPresent());
    }
}
