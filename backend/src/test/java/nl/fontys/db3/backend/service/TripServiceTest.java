package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.TripCompleteRequestDTO;
import nl.fontys.db3.backend.dto.TripDTO;
import nl.fontys.db3.backend.entity.Trip;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.mapper.TripMapper;
import nl.fontys.db3.backend.repository.TripRepository;
import nl.fontys.db3.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepo;
    @Mock private UserRepository userRepo;
    @Mock private StatisticsService statisticsService;
    @Mock private TripMapper tripMapper;

    @InjectMocks
    private TripService service;

    @Test
    void completeSoloTrip_success_savesTrip_incrementsStats_andReturnsDto() {
        // Arrange
        String email = "user@test.com";

        TripCompleteRequestDTO dto = mock(TripCompleteRequestDTO.class);
        when(dto.getStartLat()).thenReturn(51.44);
        when(dto.getStartLng()).thenReturn(5.48);
        when(dto.getEndLat()).thenReturn(51.45);
        when(dto.getEndLng()).thenReturn(5.49);
        when(dto.getDistanceKm()).thenReturn(12.5);

        OffsetDateTime startedAt = OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime endedAt   = OffsetDateTime.of(2026, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC);
        when(dto.getStartedAt()).thenReturn(startedAt);
        when(dto.getEndedAt()).thenReturn(endedAt);

        User user = mock(User.class);
        when(user.getId()).thenReturn(7L);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        when(tripRepo.save(tripCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        TripDTO mappedDto = mock(TripDTO.class);
        when(tripMapper.toDTO(any(Trip.class))).thenReturn(mappedDto);

        // Act
        TripDTO result = service.completeSoloTrip(email, dto);

        // Assert
        assertNotNull(result);
        assertSame(mappedDto, result);

        Trip savedTrip = tripCaptor.getValue();
        assertNotNull(savedTrip);
        assertSame(user, savedTrip.getUser());
        assertNull(savedTrip.getConvoyId(), "Solo trip should have convoyId null");

        assertEquals(51.44, savedTrip.getStartLat());
        assertEquals(5.48, savedTrip.getStartLng());
        assertEquals(51.45, savedTrip.getEndLat());
        assertEquals(5.49, savedTrip.getEndLng());
        assertEquals(12.5, savedTrip.getDistanceKm());
        assertEquals(startedAt, savedTrip.getStartedAt());
        assertEquals(endedAt, savedTrip.getEndedAt());

        verify(statisticsService).incrementTripsAndDistance(7L, 12.5);
        verify(tripMapper).toDTO(any(Trip.class));
    }

    @Test
    void completeSoloTrip_nullBody_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.completeSoloTrip("user@test.com", null));

        verifyNoInteractions(userRepo, tripRepo, statisticsService, tripMapper);
    }

    @Test
    void completeSoloTrip_missingStartLat_throws() {
        TripCompleteRequestDTO dto = mock(TripCompleteRequestDTO.class);
        when(dto.getStartLat()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.completeSoloTrip("user@test.com", dto));

        verifyNoInteractions(userRepo, tripRepo, statisticsService, tripMapper);
    }

    @Test
    void completeSoloTrip_negativeDistance_throws() {
        TripCompleteRequestDTO dto = mock(TripCompleteRequestDTO.class);
        when(dto.getStartLat()).thenReturn(1.0);
        when(dto.getStartLng()).thenReturn(1.0);
        when(dto.getEndLat()).thenReturn(1.0);
        when(dto.getEndLng()).thenReturn(1.0);
        when(dto.getDistanceKm()).thenReturn(-0.1);

        assertThrows(IllegalArgumentException.class,
                () -> service.completeSoloTrip("user@test.com", dto));

        verifyNoInteractions(userRepo, tripRepo, statisticsService, tripMapper);
    }

    @Test
    void completeSoloTrip_startedAtNull_throws() {
        TripCompleteRequestDTO dto = mock(TripCompleteRequestDTO.class);
        when(dto.getStartLat()).thenReturn(1.0);
        when(dto.getStartLng()).thenReturn(1.0);
        when(dto.getEndLat()).thenReturn(1.0);
        when(dto.getEndLng()).thenReturn(1.0);
        when(dto.getDistanceKm()).thenReturn(1.0);

        // This is the one we want to trigger
        when(dto.getStartedAt()).thenReturn(null);

        // DO NOT stub endedAt here — it won’t be called
        assertThrows(IllegalArgumentException.class,
                () -> service.completeSoloTrip("user@test.com", dto));

        verifyNoInteractions(userRepo, tripRepo, statisticsService, tripMapper);
    }

    @Test
    void completeSoloTrip_endedAtBeforeStartedAt_throws() {
        TripCompleteRequestDTO dto = mock(TripCompleteRequestDTO.class);
        when(dto.getStartLat()).thenReturn(1.0);
        when(dto.getStartLng()).thenReturn(1.0);
        when(dto.getEndLat()).thenReturn(1.0);
        when(dto.getEndLng()).thenReturn(1.0);
        when(dto.getDistanceKm()).thenReturn(1.0);

        OffsetDateTime started = OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime ended   = OffsetDateTime.of(2026, 1, 1, 9, 59, 0, 0, ZoneOffset.UTC);
        when(dto.getStartedAt()).thenReturn(started);
        when(dto.getEndedAt()).thenReturn(ended);

        assertThrows(IllegalArgumentException.class,
                () -> service.completeSoloTrip("user@test.com", dto));

        verifyNoInteractions(userRepo, tripRepo, statisticsService, tripMapper);
    }

    @Test
    void completeSoloTrip_userNotFound_throws() {
        TripCompleteRequestDTO dto = mock(TripCompleteRequestDTO.class);
        when(dto.getStartLat()).thenReturn(1.0);
        when(dto.getStartLng()).thenReturn(1.0);
        when(dto.getEndLat()).thenReturn(1.0);
        when(dto.getEndLng()).thenReturn(1.0);
        when(dto.getDistanceKm()).thenReturn(1.0);
        when(dto.getStartedAt()).thenReturn(OffsetDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC));
        when(dto.getEndedAt()).thenReturn(OffsetDateTime.of(2026, 1, 1, 11, 0, 0, 0, ZoneOffset.UTC));

        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.completeSoloTrip("user@test.com", dto));

        verify(tripRepo, never()).save(any());
        verify(statisticsService, never()).incrementTripsAndDistance(anyLong(), anyDouble());
        verify(tripMapper, never()).toDTO(any());
    }
}
