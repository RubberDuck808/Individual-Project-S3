package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.Statistics;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.StatisticsRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class StatisticsRepositoryIT {

    @Autowired
    StatisticsRepository statisticsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setUp() {
        try {
            statisticsRepository.deleteAll();
            userRepository.deleteAll();
            roleRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }

        Role role = Role.builder().name("USER").build();
        roleRepository.save(role);

        user = User.builder()
                .username("statsuser")
                .email("stats@test.com")
                .name("Stats User")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(user);
    }

    @Test
    void saveAndFindById() {
        Statistics stats = Statistics.builder()
                .user(user)
                .totalTrips(5)
                .totalDistanceKm(100.5)
                .totalHazardsReported(10)
                .totalVotes(15)
                .build();

        Statistics saved = statisticsRepository.save(stats);
        assertNotNull(saved.getId());

        Statistics found = statisticsRepository.findById(saved.getId()).orElseThrow();
        assertEquals(5, found.getTotalTrips());
        assertEquals(100.5, found.getTotalDistanceKm());
        assertEquals(10, found.getTotalHazardsReported());
        assertEquals(15, found.getTotalVotes());
    }

    @Test
    void findByUser_Id() {
        Statistics stats = Statistics.builder()
                .user(user)
                .totalTrips(3)
                .totalDistanceKm(50.0)
                .totalHazardsReported(5)
                .totalVotes(8)
                .build();
        statisticsRepository.save(stats);

        Optional<Statistics> found = statisticsRepository.findByUser_Id(user.getId());
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getUser().getId());
    }

    @Test
    void findByUser_Username() {
        Statistics stats = Statistics.builder()
                .user(user)
                .totalTrips(2)
                .totalDistanceKm(25.0)
                .totalHazardsReported(3)
                .totalVotes(4)
                .build();
        statisticsRepository.save(stats);

        Optional<Statistics> found = statisticsRepository.findByUser_Username(user.getUsername());
        assertTrue(found.isPresent());
        assertEquals(user.getUsername(), found.get().getUser().getUsername());
    }

    @Test
    void existsByUser_Id_Exists() {
        Statistics stats = Statistics.builder()
                .user(user)
                .build();
        statisticsRepository.save(stats);

        boolean exists = statisticsRepository.existsByUser_Id(user.getId());
        assertTrue(exists);
    }

    @Test
    void existsByUser_Id_NotExists() {
        boolean exists = statisticsRepository.existsByUser_Id(user.getId());
        assertFalse(exists);
    }

    @Test
    void incVotes() {
        Statistics stats = Statistics.builder()
                .user(user)
                .totalVotes(5)
                .build();
        statisticsRepository.save(stats);

        int updated = statisticsRepository.incVotes(user.getId());
        assertEquals(1, updated);

        Statistics found = statisticsRepository.findByUser_Id(user.getId()).orElseThrow();
        assertEquals(6, found.getTotalVotes());
    }

    @Test
    void incHazards() {
        Statistics stats = Statistics.builder()
                .user(user)
                .totalHazardsReported(3)
                .build();
        statisticsRepository.save(stats);

        int updated = statisticsRepository.incHazards(user.getId());
        assertEquals(1, updated);

        Statistics found = statisticsRepository.findByUser_Id(user.getId()).orElseThrow();
        assertEquals(4, found.getTotalHazardsReported());
    }

    @Test
    void incTripsAndAddDistance() {
        Statistics stats = Statistics.builder()
                .user(user)
                .totalTrips(2)
                .totalDistanceKm(30.0)
                .build();
        statisticsRepository.save(stats);

        int updated = statisticsRepository.incTripsAndAddDistance(user.getId(), 15.5);
        assertEquals(1, updated);

        Statistics found = statisticsRepository.findByUser_Id(user.getId()).orElseThrow();
        assertEquals(3, found.getTotalTrips());
        assertEquals(45.5, found.getTotalDistanceKm(), 0.01);
    }

    @Test
    void uniqueConstraint_User() {
        Statistics stats1 = Statistics.builder()
                .user(user)
                .build();
        statisticsRepository.save(stats1);

        Statistics stats2 = Statistics.builder()
                .user(user)
                .build();

        assertThrows(Exception.class, () -> statisticsRepository.saveAndFlush(stats2));
    }
}
