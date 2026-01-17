package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class HazardReportRepositoryIT {

    @Autowired
    HazardReportRepository hazardReportRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    HazardCategoryRepository hazardCategoryRepository;

    private User user;
    private HazardCategory category;

    @BeforeEach
    void setUp() {
        try {
            hazardReportRepository.deleteAll();
            userRepository.deleteAll();
            hazardCategoryRepository.deleteAll();
            roleRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }

        Role role = Role.builder().name("USER").build();
        roleRepository.save(role);

        user = User.builder()
                .username("hazarduser")
                .email("hazard@test.com")
                .name("Hazard User")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(user);

        category = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.svg")
                .active(true)
                .build();
        hazardCategoryRepository.save(category);
    }

    @Test
    void saveAndFindById() {
        HazardReport hazard = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();

        HazardReport saved = hazardReportRepository.save(hazard);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        HazardReport found = hazardReportRepository.findById(saved.getId()).orElseThrow();
        assertEquals(51.4416, found.getLatitude());
        assertEquals(5.4697, found.getLongitude());
        assertEquals(HazardStatus.OPEN, found.getStatus());
    }

    @Test
    void findByCreatedBy_Id() {
        HazardReport hazard1 = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard1);

        HazardReport hazard2 = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard2);

        List<HazardReport> hazards = hazardReportRepository.findByCreatedBy_Id(user.getId());
        assertEquals(2, hazards.size());
    }

    @Test
    void findByCategory_Name() {
        HazardCategory category2 = HazardCategory.builder()
                .name("Debris")
                .iconPath("/icons/debris.svg")
                .active(true)
                .build();
        hazardCategoryRepository.save(category2);

        HazardReport hazard1 = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard1);

        HazardReport hazard2 = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category2)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard2);

        List<HazardReport> potholes = hazardReportRepository.findByCategory_Name("Pothole");
        assertEquals(1, potholes.size());
        assertEquals("Pothole", potholes.get(0).getCategory().getName());
    }

    @Test
    void findByStatus() {
        HazardReport openHazard = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(openHazard);

        HazardReport resolvedHazard = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.RESOLVED)
                .build();
        hazardReportRepository.save(resolvedHazard);

        List<HazardReport> openHazards = hazardReportRepository.findByStatus(HazardStatus.OPEN);
        assertEquals(1, openHazards.size());
        assertEquals(HazardStatus.OPEN, openHazards.get(0).getStatus());
    }

    @Test
    void findByStatusIn() {
        HazardReport openHazard = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(openHazard);

        HazardReport resolvedHazard = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.RESOLVED)
                .build();
        hazardReportRepository.save(resolvedHazard);

        List<HazardReport> activeHazards = hazardReportRepository.findByStatusIn(
                List.of(HazardStatus.OPEN)
        );
        assertEquals(1, activeHazards.size());
    }

    @Test
    void findByCreatedAtAfter() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);

        HazardReport oldHazard = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .createdAt(cutoff.minusHours(2))
                .build();
        hazardReportRepository.save(oldHazard);

        HazardReport newHazard = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
        hazardReportRepository.save(newHazard);

        List<HazardReport> recentHazards = hazardReportRepository.findByCreatedAtAfter(cutoff);
        assertEquals(1, recentHazards.size());
        assertEquals(newHazard.getId(), recentHazards.get(0).getId());
    }

    @Test
    void findByCreatedByUsernameOrderByIdDesc() {
        HazardReport hazard1 = HazardReport.builder()
                .latitude(51.4416)
                .longitude(5.4697)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard1);

        HazardReport hazard2 = HazardReport.builder()
                .latitude(51.4500)
                .longitude(5.4800)
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();
        hazardReportRepository.save(hazard2);

        List<HazardReport> hazards = hazardReportRepository.findByCreatedByUsernameOrderByIdDesc(user.getUsername());
        assertEquals(2, hazards.size());
        assertTrue(hazards.get(0).getId() > hazards.get(1).getId());
    }
}
