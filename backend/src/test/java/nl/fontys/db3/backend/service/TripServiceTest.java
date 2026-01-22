package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.TripCompleteRequestDTO;
import nl.fontys.db3.backend.dto.TripDTO;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.Trip;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.mapper.TripMapper;
import nl.fontys.db3.backend.repository.TripRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private TripMapper tripMapper;

    @InjectMocks
    private TripService tripService;

    private User testUser;
    private TripCompleteRequestDTO validDto;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().id(1L).name("USER").build();
        testUser = User.builder()
                .id(1L)
                .username("tripper")
                .email("tripper@test.com")
                .name("Trip User")
                .password("encoded")
                .role(userRole)
                .build();

        validDto = TripCompleteRequestDTO.builder()
                .startLat(51.4416)
                .startLng(5.4697)
                .endLat(51.4500)
                .endLng(5.4800)
                .distanceKm(5.0)
                .startedAt(OffsetDateTime.now().minusHours(1))
                .endedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void completeSoloTrip_success() {
        // Given
        TripDTO tripDTO = TripDTO.builder()
                .id(1L)
                .userId(testUser.getId())
                .convoyId(null)
                .startLat(validDto.getStartLat())
                .startLng(validDto.getStartLng())
                .endLat(validDto.getEndLat())
                .endLng(validDto.getEndLng())
                .distanceKm(validDto.getDistanceKm())
                .startedAt(validDto.getStartedAt())
                .endedAt(validDto.getEndedAt())
                .build();

        when(userRepository.findByEmail("tripper@test.com")).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(tripMapper.toDTO(any(Trip.class))).thenReturn(tripDTO);

        // When
        TripDTO result = tripService.completeSoloTrip("tripper@test.com", validDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findByEmail("tripper@test.com");
        verify(tripRepository).save(any(Trip.class));
        verify(statisticsService).incrementTripsAndDistance(testUser.getId(), validDto.getDistanceKm());
        verify(tripMapper).toDTO(any(Trip.class));
    }


    @Test
    void completeSoloTrip_zeroDistanceKm() {
        // Given
        TripCompleteRequestDTO dto = TripCompleteRequestDTO.builder()
                .startLat(validDto.getStartLat())
                .startLng(validDto.getStartLng())
                .endLat(validDto.getEndLat())
                .endLng(validDto.getEndLng())
                .distanceKm(0.0)
                .startedAt(validDto.getStartedAt())
                .endedAt(validDto.getEndedAt())
                .build();
        TripDTO tripDTO = TripDTO.builder()
                .id(1L)
                .userId(testUser.getId())
                .convoyId(null)
                .startLat(dto.getStartLat())
                .startLng(dto.getStartLng())
                .endLat(dto.getEndLat())
                .endLng(dto.getEndLng())
                .distanceKm(dto.getDistanceKm())
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .build();

        when(userRepository.findByEmail("tripper@test.com")).thenReturn(Optional.of(testUser));
        when(tripRepository.save(any(Trip.class))).thenAnswer(invocation -> {
            Trip t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        when(tripMapper.toDTO(any(Trip.class))).thenReturn(tripDTO);

        // When - zero distance should be allowed (boundary value)
        TripDTO result = tripService.completeSoloTrip("tripper@test.com", dto);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getDistanceKm());
    }

}
