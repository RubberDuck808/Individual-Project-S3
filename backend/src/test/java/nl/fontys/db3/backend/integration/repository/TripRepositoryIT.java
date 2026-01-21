package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.Trip;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.TripRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class TripRepositoryIT {

    @Autowired
    TripRepository tripRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setUp() {
        try {
            tripRepository.deleteAll();
            userRepository.deleteAll();
            // Don't delete roles - migration V4__Seed_roles.sql creates them
        } catch (Exception ignored) {
            // Tables may not exist yet
        }

        // Use existing USER role from migration
        Role role = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        user = User.builder()
                .username("tripuser")
                .email("trip@test.com")
                .name("Trip User")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(user);
    }

    @Test
    void saveAndFindById() {
        OffsetDateTime now = OffsetDateTime.now();
        Trip trip = Trip.builder()
                .user(user)
                .startLat(51.4416)
                .startLng(5.4697)
                .endLat(51.4500)
                .endLng(5.4800)
                .distanceKm(15.5)
                .startedAt(now.minusHours(1))
                .endedAt(now)
                .build();

        Trip saved = tripRepository.save(trip);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        Trip found = tripRepository.findById(saved.getId()).orElseThrow();
        assertEquals(51.4416, found.getStartLat());
        assertEquals(5.4697, found.getStartLng());
        assertEquals(51.4500, found.getEndLat());
        assertEquals(5.4800, found.getEndLng());
        assertEquals(15.5, found.getDistanceKm());
    }

    @Test
    void findByUser_UsernameOrderByIdDesc() {
        OffsetDateTime now = OffsetDateTime.now();

        Trip trip1 = Trip.builder()
                .user(user)
                .startLat(51.4416)
                .startLng(5.4697)
                .endLat(51.4500)
                .endLng(5.4800)
                .distanceKm(10.0)
                .startedAt(now.minusHours(2))
                .endedAt(now.minusHours(1))
                .build();
        tripRepository.save(trip1);

        Trip trip2 = Trip.builder()
                .user(user)
                .startLat(51.4500)
                .startLng(5.4800)
                .endLat(51.4600)
                .endLng(5.4900)
                .distanceKm(20.0)
                .startedAt(now.minusHours(1))
                .endedAt(now)
                .build();
        tripRepository.save(trip2);

        List<Trip> trips = tripRepository.findByUser_UsernameOrderByIdDesc(user.getUsername());
        assertEquals(2, trips.size());
        assertTrue(trips.get(0).getId() > trips.get(1).getId());
    }

    @Test
    void findByUser_UsernameOrderByIdDesc_Empty() {
        List<Trip> trips = tripRepository.findByUser_UsernameOrderByIdDesc(user.getUsername());
        assertTrue(trips.isEmpty());
    }

    @Test
    void convoyId() {
        OffsetDateTime now = OffsetDateTime.now();
        Trip trip = Trip.builder()
                .user(user)
                .startLat(51.4416)
                .startLng(5.4697)
                .endLat(51.4500)
                .endLng(5.4800)
                .distanceKm(15.5)
                .startedAt(now.minusHours(1))
                .endedAt(now)
                .convoyId(123L)
                .build();

        Trip saved = tripRepository.save(trip);
        assertEquals(123L, saved.getConvoyId());
    }
}
