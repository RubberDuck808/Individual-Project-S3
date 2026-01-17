package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.repository.BackgroundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class BackgroundRepositoryIT {

    @Autowired
    BackgroundRepository backgroundRepository;

    @BeforeEach
    void setUp() {
        try {
            backgroundRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }
    }

    @Test
    void saveAndFindById() {
        Background background = Background.builder()
                .name("Ocean Blue")
                .imagePath("backgrounds/ocean-blue.png")
                .active(true)
                .build();

        Background saved = backgroundRepository.save(background);
        assertNotNull(saved.getId());

        Background found = backgroundRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Ocean Blue", found.getName());
        assertEquals("backgrounds/ocean-blue.png", found.getImagePath());
        assertTrue(found.isActive());
    }

    @Test
    void findAllByActiveTrueOrderByNameAsc() {
        Background active1 = Background.builder()
                .name("Zebra")
                .imagePath("backgrounds/zebra.png")
                .active(true)
                .build();
        backgroundRepository.save(active1);

        Background active2 = Background.builder()
                .name("Alpha")
                .imagePath("backgrounds/alpha.png")
                .active(true)
                .build();
        backgroundRepository.save(active2);

        Background inactive = Background.builder()
                .name("Beta")
                .imagePath("backgrounds/beta.png")
                .active(false)
                .build();
        backgroundRepository.save(inactive);

        List<Background> activeBackgrounds = backgroundRepository.findAllByActiveTrueOrderByNameAsc();
        assertEquals(2, activeBackgrounds.size());
        assertEquals("Alpha", activeBackgrounds.get(0).getName());
        assertEquals("Zebra", activeBackgrounds.get(1).getName());
    }

    @Test
    void existsByImagePath_Exists() {
        Background background = Background.builder()
                .name("Test")
                .imagePath("backgrounds/test.png")
                .active(true)
                .build();
        backgroundRepository.save(background);

        boolean exists = backgroundRepository.existsByImagePath("backgrounds/test.png");
        assertTrue(exists);
    }

    @Test
    void existsByImagePath_NotExists() {
        boolean exists = backgroundRepository.existsByImagePath("backgrounds/nonexistent.png");
        assertFalse(exists);
    }

    @Test
    void existsByName_Exists() {
        Background background = Background.builder()
                .name("Unique Name")
                .imagePath("backgrounds/unique.png")
                .active(true)
                .build();
        backgroundRepository.save(background);

        boolean exists = backgroundRepository.existsByName("Unique Name");
        assertTrue(exists);
    }

    @Test
    void existsByName_NotExists() {
        boolean exists = backgroundRepository.existsByName("Nonexistent");
        assertFalse(exists);
    }

    @Test
    void findByNameIgnoreCaseAndActiveTrue() {
        Background background = Background.builder()
                .name("Test Background")
                .imagePath("backgrounds/test.png")
                .active(true)
                .build();
        backgroundRepository.save(background);

        Optional<Background> found = backgroundRepository.findByNameIgnoreCaseAndActiveTrue("test background");
        assertTrue(found.isPresent());
        assertEquals("Test Background", found.get().getName());
    }

    @Test
    void findByNameIgnoreCaseAndActiveTrue_Inactive() {
        Background background = Background.builder()
                .name("Inactive Background")
                .imagePath("backgrounds/inactive.png")
                .active(false)
                .build();
        backgroundRepository.save(background);

        Optional<Background> found = backgroundRepository.findByNameIgnoreCaseAndActiveTrue("inactive background");
        assertFalse(found.isPresent());
    }
}
