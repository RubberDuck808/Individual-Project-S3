package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.UserStatsDTO;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.Statistics;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.StatisticsRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StatisticsRepository statsRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private StatisticsService statisticsService;

    private User testUser;
    private Statistics testStats;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").build();
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .name("Test User")
                .password("encoded")
                .role(userRole)
                .build();

        testStats = Statistics.builder()
                .id(1L)
                .user(testUser)
                .totalTrips(10)
                .totalDistanceKm(100.5)
                .totalHazardsReported(5)
                .totalVotes(20)
                .build();
    }

    // Tests for ensureStatsRow
    @Test
    void ensureStatsRow_existingStats() {
        when(statsRepo.findByUser_Id(1L)).thenReturn(Optional.of(testStats));

        Statistics result = statisticsService.ensureStatsRow(testUser);

        assertNotNull(result);
        assertEquals(testStats, result);
        verify(statsRepo).findByUser_Id(1L);
        verify(statsRepo, never()).save(any(Statistics.class));
    }

    @Test
    void ensureStatsRow_createNew() {
        when(statsRepo.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(statsRepo.save(any(Statistics.class))).thenAnswer(invocation -> {
            Statistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return stats;
        });

        Statistics result = statisticsService.ensureStatsRow(testUser);

        assertNotNull(result);
        assertEquals(0, result.getTotalTrips());
        assertEquals(0.0, result.getTotalDistanceKm());
        assertEquals(0, result.getTotalHazardsReported());
        assertEquals(0, result.getTotalVotes());
        verify(statsRepo).save(any(Statistics.class));
    }

    // Tests for getStatsByUsername
    @Test
    void getStatsByUsername_success() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(statsRepo.findByUser_Id(1L)).thenReturn(Optional.of(testStats));

        UserStatsDTO result = statisticsService.getStatsByUsername("testuser");

        assertNotNull(result);
        assertEquals(10, result.getTotalTrips());
        assertEquals(100.5, result.getTotalDistanceKm());
        assertEquals(5, result.getTotalHazardsReported());
        assertEquals(20, result.getTotalVotes());
    }

    @Test
    void getStatsByUsername_autoCreateStats() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(statsRepo.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(statsRepo.save(any(Statistics.class))).thenAnswer(invocation -> {
            Statistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return stats;
        });

        UserStatsDTO result = statisticsService.getStatsByUsername("testuser");

        assertNotNull(result);
        assertEquals(0, result.getTotalTrips());
        assertEquals(0.0, result.getTotalDistanceKm());
        verify(statsRepo).save(any(Statistics.class));
    }

    @Test
    void getStatsByUsername_nullUsername() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.getStatsByUsername(null);
        });

        assertEquals("username cannot be null/blank", exception.getMessage());
        verify(userRepo, never()).findByUsername(anyString());
    }

    @Test
    void getStatsByUsername_blankUsername() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.getStatsByUsername("   ");
        });

        assertEquals("username cannot be null/blank", exception.getMessage());
    }

    @Test
    void getStatsByUsername_userNotFound() {
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.getStatsByUsername("nonexistent");
        });

        assertEquals("User not found", exception.getMessage());
    }

    // Tests for incrementVotes
    @Test
    void incrementVotes_success() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(true);
        when(statsRepo.incVotes(1L)).thenReturn(1);

        assertDoesNotThrow(() -> statisticsService.incrementVotes(1L));

        verify(statsRepo).existsByUser_Id(1L);
        verify(statsRepo).incVotes(1L);
        verify(statsRepo, never()).save(any(Statistics.class));
    }

    @Test
    void incrementVotes_createStatsFirst() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(statsRepo.save(any(Statistics.class))).thenAnswer(invocation -> {
            Statistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return stats;
        });
        when(statsRepo.incVotes(1L)).thenReturn(1);

        assertDoesNotThrow(() -> statisticsService.incrementVotes(1L));

        verify(statsRepo).save(any(Statistics.class));
        verify(statsRepo).flush();
        verify(statsRepo).incVotes(1L);
    }

    @Test
    void incrementVotes_userNotFound() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.incrementVotes(1L);
        });

        assertEquals("User not found for userId=1", exception.getMessage());
        verify(statsRepo, never()).incVotes(anyLong());
    }

    @Test
    void incrementVotes_updateFailed() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(true);
        when(statsRepo.incVotes(1L)).thenReturn(0);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            statisticsService.incrementVotes(1L);
        });

        assertEquals("Statistics row missing for userId=1", exception.getMessage());
    }

    // Tests for incrementHazards
    @Test
    void incrementHazards_success() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(true);
        when(statsRepo.incHazards(1L)).thenReturn(1);

        assertDoesNotThrow(() -> statisticsService.incrementHazards(1L));

        verify(statsRepo).existsByUser_Id(1L);
        verify(statsRepo).incHazards(1L);
        verify(statsRepo, never()).save(any(Statistics.class));
    }

    @Test
    void incrementHazards_createStatsFirst() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(statsRepo.save(any(Statistics.class))).thenAnswer(invocation -> {
            Statistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return stats;
        });
        when(statsRepo.incHazards(1L)).thenReturn(1);

        assertDoesNotThrow(() -> statisticsService.incrementHazards(1L));

        verify(statsRepo).save(any(Statistics.class));
        verify(statsRepo).flush();
        verify(statsRepo).incHazards(1L);
    }

    @Test
    void incrementHazards_userNotFound() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.incrementHazards(1L);
        });

        assertEquals("User not found for userId=1", exception.getMessage());
        verify(statsRepo, never()).incHazards(anyLong());
    }

    @Test
    void incrementHazards_updateFailed() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(true);
        when(statsRepo.incHazards(1L)).thenReturn(0);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            statisticsService.incrementHazards(1L);
        });

        assertEquals("Statistics row missing for userId=1", exception.getMessage());
    }

    // Tests for incrementTripsAndDistance
    @Test
    void incrementTripsAndDistance_success() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(true);
        when(statsRepo.incTripsAndAddDistance(1L, 50.5)).thenReturn(1);

        assertDoesNotThrow(() -> statisticsService.incrementTripsAndDistance(1L, 50.5));

        verify(statsRepo).existsByUser_Id(1L);
        verify(statsRepo).incTripsAndAddDistance(1L, 50.5);
        verify(statsRepo, never()).save(any(Statistics.class));
    }

    @Test
    void incrementTripsAndDistance_createStatsFirst() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(statsRepo.save(any(Statistics.class))).thenAnswer(invocation -> {
            Statistics stats = invocation.getArgument(0);
            stats.setId(1L);
            return stats;
        });
        when(statsRepo.incTripsAndAddDistance(1L, 50.5)).thenReturn(1);

        assertDoesNotThrow(() -> statisticsService.incrementTripsAndDistance(1L, 50.5));

        verify(statsRepo).save(any(Statistics.class));
        verify(statsRepo).flush();
        verify(statsRepo).incTripsAndAddDistance(1L, 50.5);
    }

    @Test
    void incrementTripsAndDistance_negativeDistance() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.incrementTripsAndDistance(1L, -10.0);
        });

        assertEquals("distanceKm cannot be negative", exception.getMessage());
        verify(statsRepo, never()).incTripsAndAddDistance(anyLong(), anyDouble());
    }

    @Test
    void incrementTripsAndDistance_zeroDistance() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(true);
        when(statsRepo.incTripsAndAddDistance(1L, 0.0)).thenReturn(1);

        assertDoesNotThrow(() -> statisticsService.incrementTripsAndDistance(1L, 0.0));

        verify(statsRepo).incTripsAndAddDistance(1L, 0.0);
    }

    @Test
    void incrementTripsAndDistance_userNotFound() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            statisticsService.incrementTripsAndDistance(1L, 50.5);
        });

        assertEquals("User not found for userId=1", exception.getMessage());
        verify(statsRepo, never()).incTripsAndAddDistance(anyLong(), anyDouble());
    }

    @Test
    void incrementTripsAndDistance_updateFailed() {
        when(statsRepo.existsByUser_Id(1L)).thenReturn(true);
        when(statsRepo.incTripsAndAddDistance(1L, 50.5)).thenReturn(0);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            statisticsService.incrementTripsAndDistance(1L, 50.5);
        });

        assertEquals("Statistics row missing for userId=1", exception.getMessage());
    }
}
