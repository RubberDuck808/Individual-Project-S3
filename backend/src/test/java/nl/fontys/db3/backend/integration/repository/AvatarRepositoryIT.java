package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.repository.AvatarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
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
class AvatarRepositoryIT {

    @Autowired
    AvatarRepository avatarRepository;

    @BeforeEach
    void setUp() {
        try {
            avatarRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }
    }

    @Test
    void saveAndFindById() {
        Avatar avatar = Avatar.builder()
                .name("Robot Blue")
                .imagePath("avatars/robot-blue.png")
                .active(true)
                .build();

        Avatar saved = avatarRepository.save(avatar);
        assertNotNull(saved.getId());

        Avatar found = avatarRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Robot Blue", found.getName());
        assertEquals("avatars/robot-blue.png", found.getImagePath());
        assertTrue(found.isActive());
    }

    @Test
    void findAllByActiveTrueOrderByNameAsc() {
        Avatar active1 = Avatar.builder()
                .name("Zebra")
                .imagePath("avatars/zebra.png")
                .active(true)
                .build();
        avatarRepository.save(active1);

        Avatar active2 = Avatar.builder()
                .name("Alpha")
                .imagePath("avatars/alpha.png")
                .active(true)
                .build();
        avatarRepository.save(active2);

        Avatar inactive = Avatar.builder()
                .name("Beta")
                .imagePath("avatars/beta.png")
                .active(false)
                .build();
        avatarRepository.save(inactive);

        List<Avatar> activeAvatars = avatarRepository.findAllByActiveTrueOrderByNameAsc();
        assertEquals(2, activeAvatars.size());
        assertEquals("Alpha", activeAvatars.get(0).getName());
        assertEquals("Zebra", activeAvatars.get(1).getName());
    }

    @Test
    void existsByImagePath_Exists() {
        Avatar avatar = Avatar.builder()
                .name("Test")
                .imagePath("avatars/test.png")
                .active(true)
                .build();
        avatarRepository.save(avatar);

        boolean exists = avatarRepository.existsByImagePath("avatars/test.png");
        assertTrue(exists);
    }

    @Test
    void existsByImagePath_NotExists() {
        boolean exists = avatarRepository.existsByImagePath("avatars/nonexistent.png");
        assertFalse(exists);
    }

    @Test
    void existsByName_Exists() {
        Avatar avatar = Avatar.builder()
                .name("Unique Name")
                .imagePath("avatars/unique.png")
                .active(true)
                .build();
        avatarRepository.save(avatar);

        boolean exists = avatarRepository.existsByName("Unique Name");
        assertTrue(exists);
    }

    @Test
    void existsByName_NotExists() {
        boolean exists = avatarRepository.existsByName("Nonexistent");
        assertFalse(exists);
    }

    @Test
    void findByNameIgnoreCaseAndActiveTrue() {
        Avatar avatar = Avatar.builder()
                .name("Test Avatar")
                .imagePath("avatars/test.png")
                .active(true)
                .build();
        avatarRepository.save(avatar);

        Optional<Avatar> found = avatarRepository.findByNameIgnoreCaseAndActiveTrue("test avatar");
        assertTrue(found.isPresent());
        assertEquals("Test Avatar", found.get().getName());
    }

    @Test
    void findByNameIgnoreCaseAndActiveTrue_Inactive() {
        Avatar avatar = Avatar.builder()
                .name("Inactive Avatar")
                .imagePath("avatars/inactive.png")
                .active(false)
                .build();
        avatarRepository.save(avatar);

        Optional<Avatar> found = avatarRepository.findByNameIgnoreCaseAndActiveTrue("inactive avatar");
        assertFalse(found.isPresent());
    }

    @Test
    void uniqueConstraint_Name() {
        Avatar avatar1 = Avatar.builder()
                .name("Duplicate")
                .imagePath("avatars/dup1.png")
                .active(true)
                .build();
        avatarRepository.save(avatar1);

        Avatar avatar2 = Avatar.builder()
                .name("Duplicate")
                .imagePath("avatars/dup2.png")
                .active(true)
                .build();

        assertThrows(Exception.class, () -> avatarRepository.saveAndFlush(avatar2));
    }

    @Test
    void uniqueConstraint_ImagePath() {
        Avatar avatar1 = Avatar.builder()
                .name("Avatar 1")
                .imagePath("avatars/same.png")
                .active(true)
                .build();
        avatarRepository.save(avatar1);

        Avatar avatar2 = Avatar.builder()
                .name("Avatar 2")
                .imagePath("avatars/same.png")
                .active(true)
                .build();

        assertThrows(Exception.class, () -> avatarRepository.saveAndFlush(avatar2));
    }
}
