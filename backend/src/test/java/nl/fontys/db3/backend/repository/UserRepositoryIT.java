package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryIT {

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;

    @Test
    void saveAndFindByEmail_works() {
        // USER exists at lowest level, ensure it exists for test DB:
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        User u = User.builder()
                .email("user@test.com")
                .username("user")
                .name("User")
                .password("encoded")
                .role(userRole)
                .build();

        userRepository.save(u);

        assertTrue(userRepository.findByEmail("user@test.com").isPresent());
    }
}
