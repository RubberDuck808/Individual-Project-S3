package nl.fontys.db3.backend.repository;


import nl.fontys.db3.backend.IntegrationTestBase;
import nl.fontys.db3.backend.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryIT extends IntegrationTestBase {

    
    @Autowired
    UserRepository userRepository;

    @Test
    void saveAndFindByEmail_works() {
        User u = new User();
        u.setEmail("user@test.com");
        u.setUsername("user");
        u.setName("User");
        u.setPassword("encoded");

        userRepository.save(u);

        assertTrue(userRepository.findByEmail("user@test.com").isPresent());
    }
}
