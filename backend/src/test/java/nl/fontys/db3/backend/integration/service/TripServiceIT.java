package nl.fontys.db3.backend.integration.service;

import nl.fontys.db3.backend.dto.TripCompleteRequestDTO;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.Statistics;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.StatisticsRepository;
import nl.fontys.db3.backend.repository.TripRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true",
    "app.jwt.secret=ci-test-secret",
    "MAPBOX_TOKEN=test"
})
@Transactional
class TripServiceIT {

    @Autowired
    private TripService tripService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private StatisticsRepository statisticsRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // With @Transactional, each test rolls back, so we just need to set up test data
        // Check if role exists, otherwise create it
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        testUser = User.builder()
                .username("tripper")
                .email("tripper@test.com")
                .name("Trip User")
                .password("encoded")
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);
    }


    @Test
    void completeSoloTrip_invalidDistance_throwsException() {
        TripCompleteRequestDTO dto = TripCompleteRequestDTO.builder()
                .startLat(51.4416)
                .startLng(5.4697)
                .endLat(51.4500)
                .endLng(5.4800)
                .distanceKm(-5.0) // Invalid negative distance
                .startedAt(OffsetDateTime.now().minusHours(1))
                .endedAt(OffsetDateTime.now())
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> tripService.completeSoloTrip("tripper@test.com", dto));

        assertEquals(0, tripRepository.count());
        Optional<Statistics> stats = statisticsRepository.findByUser_Id(testUser.getId());
        assertTrue(stats.isEmpty() || stats.get().getTotalTrips() == 0);
    }
}
