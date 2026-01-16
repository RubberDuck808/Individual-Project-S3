package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.UserStatsDTO;
import nl.fontys.db3.backend.entity.Statistics;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.StatisticsRepository;
import nl.fontys.db3.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatisticsServiceTest {

    @Mock private StatisticsRepository statsRepo;
    @Mock private UserRepository userRepo;

    @InjectMocks
    private StatisticsService service;


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


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getStatsByUsername_nullOrBlank_throws(String username) {
        assertThrows(IllegalArgumentException.class, () -> service.getStatsByUsername(username));
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

        when(statsRepo.save(any(Statistics.class))).thenAnswer(inv -> inv.getArgument(0));

        UserStatsDTO dto = service.getStatsByUsername("bob");

        assertNotNull(dto);
        assertEquals(0, dto.getTotalTrips());
        assertEquals(0.0, dto.getTotalDistanceKm());
        assertEquals(0, dto.getTotalHazardsReported());
        assertEquals(0, dto.getTotalVotes());

        verify(statsRepo).save(any(Statistics.class));
    }


    @Test
    void incrementVotes_success_whenRowExists() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(true);
        when(statsRepo.incVotes(7L)).thenReturn(1);

        service.incrementVotes(7L);

        verify(statsRepo).incVotes(7L);
        verify(statsRepo, never()).save(any());
    }

    @Test
    void incrementVotes_missingRow_autoCreatesAndIncrements() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        
        when(statsRepo.existsByUser_Id(7L)).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.of(user));
        
        when(statsRepo.save(any(Statistics.class))).thenAnswer(inv -> inv.getArgument(0));
        doReturn(1).when(statsRepo).incVotes(7L);

        service.incrementVotes(7L);

        verify(statsRepo).existsByUser_Id(7L);
        verify(userRepo).findById(7L);
        verify(statsRepo).save(any(Statistics.class));
        verify(statsRepo).incVotes(7L);
    }

    @Test
    void incrementHazards_success_whenRowExists() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(true);
        when(statsRepo.incHazards(7L)).thenReturn(1);

        service.incrementHazards(7L);

        verify(statsRepo).incHazards(7L);
        verify(statsRepo, never()).save(any());
    }

    @Test
    void incrementHazards_missingRow_autoCreatesAndIncrements() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        
        when(statsRepo.existsByUser_Id(7L)).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.of(user));
        
        when(statsRepo.save(any(Statistics.class))).thenAnswer(inv -> inv.getArgument(0));
        doReturn(1).when(statsRepo).incHazards(7L);

        service.incrementHazards(7L);

        verify(statsRepo).existsByUser_Id(7L);
        verify(userRepo).findById(7L);
        verify(statsRepo).save(any(Statistics.class));
        verify(statsRepo).incHazards(7L);
    }


    @Test
    void incrementTripsAndDistance_negativeKm_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.incrementTripsAndDistance(1L, -0.1));
        verifyNoInteractions(statsRepo);
    }

    @Test
    void incrementTripsAndDistance_success_whenRowExists() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(true);
        when(statsRepo.incTripsAndAddDistance(7L, 12.5)).thenReturn(1);

        service.incrementTripsAndDistance(7L, 12.5);

        verify(statsRepo).incTripsAndAddDistance(7L, 12.5);
        verify(statsRepo, never()).save(any());
    }

    @Test
    void incrementTripsAndDistance_missingRow_autoCreatesAndIncrements() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        
        when(statsRepo.existsByUser_Id(7L)).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.of(user));
        
        when(statsRepo.save(any(Statistics.class))).thenAnswer(inv -> inv.getArgument(0));
        doReturn(1).when(statsRepo).incTripsAndAddDistance(7L, 12.5);

        service.incrementTripsAndDistance(7L, 12.5);

        verify(statsRepo).existsByUser_Id(7L);
        verify(userRepo).findById(7L);
        verify(statsRepo).save(any(Statistics.class));
        verify(statsRepo).flush(); // Verify flush is called after creating row
        verify(statsRepo).incTripsAndAddDistance(7L, 12.5);
    }

    @Test
    void incrementVotes_userNotFound_throws() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.incrementVotes(7L));

        verify(statsRepo).existsByUser_Id(7L);
        verify(userRepo).findById(7L);
        verify(statsRepo, never()).save(any());
        verify(statsRepo, never()).incVotes(anyLong());
    }

    @Test
    void incrementVotes_updateReturnsZero_throws() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(true);
        when(statsRepo.incVotes(7L)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.incrementVotes(7L));

        verify(statsRepo).incVotes(7L);
    }

    @Test
    void incrementVotes_missingRow_flushCalled() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        
        when(statsRepo.existsByUser_Id(7L)).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.of(user));
        
        when(statsRepo.save(any(Statistics.class))).thenAnswer(inv -> inv.getArgument(0));
        doReturn(1).when(statsRepo).incVotes(7L);

        service.incrementVotes(7L);

        verify(statsRepo).flush(); // Verify flush is called after creating row
    }

    @Test
    void incrementHazards_userNotFound_throws() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.incrementHazards(7L));

        verify(statsRepo).existsByUser_Id(7L);
        verify(userRepo).findById(7L);
        verify(statsRepo, never()).save(any());
        verify(statsRepo, never()).incHazards(anyLong());
    }

    @Test
    void incrementHazards_updateReturnsZero_throws() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(true);
        when(statsRepo.incHazards(7L)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.incrementHazards(7L));

        verify(statsRepo).incHazards(7L);
    }

    @Test
    void incrementHazards_missingRow_flushCalled() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        
        when(statsRepo.existsByUser_Id(7L)).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.of(user));
        
        when(statsRepo.save(any(Statistics.class))).thenAnswer(inv -> inv.getArgument(0));
        doReturn(1).when(statsRepo).incHazards(7L);

        service.incrementHazards(7L);

        verify(statsRepo).flush(); // Verify flush is called after creating row
    }

    @Test
    void incrementTripsAndDistance_userNotFound_throws() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(false);
        when(userRepo.findById(7L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.incrementTripsAndDistance(7L, 10.0));

        verify(statsRepo).existsByUser_Id(7L);
        verify(userRepo).findById(7L);
        verify(statsRepo, never()).save(any());
        verify(statsRepo, never()).incTripsAndAddDistance(anyLong(), anyDouble());
    }

    @Test
    void incrementTripsAndDistance_updateReturnsZero_throws() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(true);
        when(statsRepo.incTripsAndAddDistance(7L, 12.5)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.incrementTripsAndDistance(7L, 12.5));

        verify(statsRepo).incTripsAndAddDistance(7L, 12.5);
    }

    @Test
    void incrementTripsAndDistance_zeroKm_success() {
        when(statsRepo.existsByUser_Id(7L)).thenReturn(true);
        when(statsRepo.incTripsAndAddDistance(7L, 0.0)).thenReturn(1);

        service.incrementTripsAndDistance(7L, 0.0);

        verify(statsRepo).incTripsAndAddDistance(7L, 0.0);
    }

    @Test
    void getStatsByUsername_emptyString_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getStatsByUsername(""));
        verifyNoInteractions(userRepo, statsRepo);
    }
}
