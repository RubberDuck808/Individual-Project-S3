package nl.fontys.db3.backend.integration.service;

import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.StatisticsRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.hazard.HazardWsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HazardServiceIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HazardReportRepository hazardReportRepository;

    @Autowired
    private HazardCategoryRepository hazardCategoryRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private HazardWsPublisher wsPublisher;

    private User testUser;
    private HazardCategory category;
    private Role userRole;

    @BeforeEach
    void setUp() {
        try {
            hazardReportRepository.deleteAll();
            statisticsRepository.deleteAll();
            userRepository.deleteAll();
            hazardCategoryRepository.deleteAll();
            roleRepository.deleteAll();
        } catch (Exception ignored) {
            // Ignore exceptions during cleanup - Flyway migrations create the schema
        }

        userRole = Role.builder().name("USER").build();
        roleRepository.save(userRole);

        testUser = User.builder()
                .username("hazardreporter")
                .email("hazard@test.com")
                .name("Hazard Reporter")
                .password("encoded")
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);

        category = HazardCategory.builder()
                .name("Pothole")
                .iconPath("/icons/pothole.png")
                .active(true)
                .build();
        category = hazardCategoryRepository.save(category);
    }

}
