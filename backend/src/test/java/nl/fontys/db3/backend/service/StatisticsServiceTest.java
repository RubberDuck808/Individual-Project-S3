package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.UserStatsDTO;
import nl.fontys.db3.backend.entity.Statistics;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.StatisticsRepository;
import nl.fontys.db3.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock private StatisticsRepository statsRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks
    private StatisticsService service;

    /* ===================== ensureStatsRow ===================== */

    @Test
    void ensureStatsRow_whenExists_returnsExisting() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Statistics existing = mock(Statistics.class);
        when(statsRepo.findByUser_Id(1L)).thenReturn(Optional.of(existing));

        Statistics result = service.ensureStatsRow(user);

        assertSame(existing, result);
        verify(statsRepo, never()).save(any());
    }

    @Test
    void ensureStatsRow_whenMissing_createsAndSavesDefaults() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        when(statsRepo.findByUser_Id(1L)).thenReturn(Optional.empty());

        ArgumentCaptor<Statistics> captor = ArgumentCaptor.forClass(Statistics.class);
        when(statsRepo.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        Statistics result = service.ensureStatsRow(user);

        assertNotNull(result);

        Statistics toSave = captor.getValue();
        assertNotNull(toSave);
        assertSame(user, toSave.getUser());
        assertEquals(0, toSave.getTotalTrips());
        assertEquals(0.0, toSave.getTotalDistanceKm());
        assertEquals(0, toSave.getTotalHazardsReported());
        assertEquals(0, toSave.getTotalVotes());

        verify(statsRepo).save(any(Statistics.class));
    }

    /* ===================== getStatsByUsername ===================== */

    @Test
    void getStatsByUsername_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getStatsByUsername("  "));
        verifyNoInteractions(userRepo, statsRepo);
    }

    @Test
    void getStatsByUsername_userNotFound_throws() {
        when(userRepo.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getStatsByUsername("bob"));

        verify(userRepo).findByUsername("bob");
        verifyNoInteractions(statsRepo);
    }

    @Test
    void getStatsByUsername_success_returnsDtoFromExistingStats() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(5L);
        when(userRepo.findByUsername("bob")).thenReturn(Optional.of(user));

        Statistics s = mock(Statistics.class);
        when(s.getTotalTrips()).thenReturn(3);
        when(s.getTotalDistanceKm()).thenReturn(12.5);
        when(s.getTotalHazardsReported()).thenReturn(2);
        when(s.getTotalVotes()).thenReturn(9);

        when(statsRepo.findByUser_Id(5L)).thenReturn(Optional.of(s));

        UserStatsDTO dto = service.getStatsByUsername("bob");

        assertNotNull(dto);
        assertEquals(3, dto.getTotalTrips());
        assertEquals(12.5, dto.getTotalDistanceKm());
        assertEquals(2, dto.getTotalHazardsReported());
        assertEquals(9, dto.getTotalVotes());

        verify(statsRepo).findByUser_Id(5L);
        verify(statsRepo, never()).save(any());
    }

    @Test
    void getStatsByUsername_statsMissing_autoHealsByCreatingRow() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(5L);
        when(userRepo.findByUsername("bob")).thenReturn(Optional.of(user));

        when(statsRepo.findByUser_Id(5L)).thenReturn(Optional.empty());

        // when ensureStatsRow saves new row, return a stats object with defaults
        Statistics created = Statistics.builder()
                .user(user)
                .totalTrips(0)
                .totalDistanceKm(0.0)
                .totalHazardsReported(0)
                .totalVotes(0)
                .build();
        when(statsRepo.save(any(Statistics.class))).thenReturn(created);

        UserStatsDTO dto = service.getStatsByUsername("bob");

        assertNotNull(dto);
        assertEquals(0, dto.getTotalTrips());
        assertEquals(0.0, dto.getTotalDistanceKm());
        assertEquals(0, dto.getTotalHazardsReported());
        assertEquals(0, dto.getTotalVotes());

        verify(statsRepo).save(any(Statistics.class));
    }

    /* ===================== incrementVotes / Hazards ===================== */

    @Test
    void incrementVotes_success_whenRowExists() {
        when(statsRepo.incVotes(7L)).thenReturn(1);

        service.incrementVotes(7L);

        verify(statsRepo).incVotes(7L);
    }

    @Test
    void incrementVotes_missingRow_throws() {
        when(statsRepo.incVotes(7L)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.incrementVotes(7L));
    }

    @Test
    void incrementHazards_success_whenRowExists() {
        when(statsRepo.incHazards(7L)).thenReturn(1);

        service.incrementHazards(7L);

        verify(statsRepo).incHazards(7L);
    }

    @Test
    void incrementHazards_missingRow_throws() {
        when(statsRepo.incHazards(7L)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.incrementHazards(7L));
    }

    /* ===================== incrementTripsAndDistance ===================== */

    @Test
    void incrementTripsAndDistance_negativeKm_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.incrementTripsAndDistance(1L, -0.1));
        verifyNoInteractions(statsRepo);
    }

    @Test
    void incrementTripsAndDistance_success_whenRowExists() {
        when(statsRepo.incTripsAndAddDistance(7L, 12.5)).thenReturn(1);

        service.incrementTripsAndDistance(7L, 12.5);

        verify(statsRepo).incTripsAndAddDistance(7L, 12.5);
    }

    @Test
    void incrementTripsAndDistance_missingRow_throws() {
        when(statsRepo.incTripsAndAddDistance(7L, 12.5)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.incrementTripsAndDistance(7L, 12.5));
    }
}
