package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

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
class HazardCategoryRepositoryIT {

    @Autowired
    HazardCategoryRepository hazardCategoryRepository;

    @BeforeEach
    void setUp() {
        try {
            hazardCategoryRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }
    }

    @Test
    void saveAndFindById() {
        HazardCategory category = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.svg")
                .active(true)
                .build();

        HazardCategory saved = hazardCategoryRepository.save(category);
        assertNotNull(saved.getId());

        HazardCategory found = hazardCategoryRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Pothole", found.getName());
        assertEquals("/icons/pothole.svg", found.getIconPath());
        assertTrue(found.isActive());
    }

    @Test
    void findAll() {
        HazardCategory category1 = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.svg")
                .active(true)
                .build();
        hazardCategoryRepository.save(category1);

        HazardCategory category2 = HazardCategory.builder()
                .name("Debris")
                .iconPath("/icons/debris.svg")
                .active(true)
                .build();
        hazardCategoryRepository.save(category2);

        assertEquals(2, hazardCategoryRepository.findAll().size());
    }

    @Test
    void duplicateNamesAllowed() {
        // Note: There is no unique constraint on name, so duplicate names are allowed
        HazardCategory category1 = HazardCategory.builder()
                .name("Duplicate")
                .iconPath("/icons/dup1.svg")
                .active(true)
                .build();
        hazardCategoryRepository.save(category1);

        HazardCategory category2 = HazardCategory.builder()
                .name("Duplicate")
                .iconPath("/icons/dup2.svg")
                .active(true)
                .build();

        // Should save successfully since there's no unique constraint
        HazardCategory saved = hazardCategoryRepository.saveAndFlush(category2);
        assertNotNull(saved.getId());
        assertEquals("Duplicate", saved.getName());
    }

    @Test
    void updateCategory() {
        HazardCategory category = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.svg")
                .active(true)
                .build();
        HazardCategory saved = hazardCategoryRepository.save(category);

        saved.setActive(false);
        saved.setIconPath("/icons/pothole-new.svg");
        HazardCategory updated = hazardCategoryRepository.save(saved);

        assertFalse(updated.isActive());
        assertEquals("/icons/pothole-new.svg", updated.getIconPath());
    }
}
