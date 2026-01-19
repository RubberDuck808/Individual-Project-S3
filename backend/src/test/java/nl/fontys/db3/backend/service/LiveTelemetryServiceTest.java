package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.LiveTelemetryDTO;
import nl.fontys.db3.backend.dto.LiveTelemetryRequestDTO;
import nl.fontys.db3.backend.entity.LiveTelemetry;
import nl.fontys.db3.backend.mapper.LiveTelemetryMapper;
import nl.fontys.db3.backend.repository.LiveTelemetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveTelemetryServiceTest {

    @Mock
    private LiveTelemetryRepository liveTelemetryRepository;

    @Mock
    private LiveTelemetryMapper liveTelemetryMapper;

    @InjectMocks
    private LiveTelemetryService liveTelemetryService;

    private LiveTelemetryRequestDTO requestDTO;
    private LiveTelemetry existingEntity;
    private LiveTelemetry newEntity;
    private LiveTelemetryDTO liveTelemetryDTO;

    @BeforeEach
    void setUp() {
        requestDTO = LiveTelemetryRequestDTO.builder()
                .deviceId("TEST-DEVICE-001")
                .speedKph(60.0)
                .rpm(2500.0)
                .latitude(52.0)
                .longitude(5.0)
                .build();

        existingEntity = LiveTelemetry.builder()
                .id(1L)
                .deviceId("TEST-DEVICE-001")
                .lastUpdated(Instant.now().minusSeconds(60))
                .speedKph(50.0)
                .rpm(2000.0)
                .build();

        newEntity = LiveTelemetry.builder()
                .deviceId("TEST-DEVICE-001")
                .lastUpdated(Instant.now())
                .speedKph(60.0)
                .rpm(2500.0)
                .latitude(52.0)
                .longitude(5.0)
                .build();

        liveTelemetryDTO = LiveTelemetryDTO.builder()
                .deviceId("TEST-DEVICE-001")
                .lastUpdated(Instant.now())
                .speedKph(60.0)
                .rpm(2500.0)
                .latitude(52.0)
                .longitude(5.0)
                .build();
    }

    @Test
    void upsert_createNew_success() {
        when(liveTelemetryRepository.findByDeviceId("TEST-DEVICE-001")).thenReturn(Optional.empty());
        when(liveTelemetryMapper.toEntity(requestDTO)).thenReturn(newEntity);
        when(liveTelemetryRepository.save(newEntity)).thenReturn(newEntity);
        when(liveTelemetryMapper.toDTO(newEntity)).thenReturn(liveTelemetryDTO);

        LiveTelemetryDTO result = liveTelemetryService.upsert(requestDTO);

        assertNotNull(result);
        assertEquals("TEST-DEVICE-001", result.getDeviceId());
        verify(liveTelemetryRepository).findByDeviceId("TEST-DEVICE-001");
        verify(liveTelemetryMapper).toEntity(requestDTO);
        verify(liveTelemetryMapper, never()).updateEntity(any(), any());
        verify(liveTelemetryRepository).save(newEntity);
        verify(liveTelemetryMapper).toDTO(newEntity);
    }

    @Test
    void upsert_updateExisting_success() {
        when(liveTelemetryRepository.findByDeviceId("TEST-DEVICE-001")).thenReturn(Optional.of(existingEntity));
        when(liveTelemetryRepository.save(existingEntity)).thenReturn(existingEntity);
        when(liveTelemetryMapper.toDTO(existingEntity)).thenReturn(liveTelemetryDTO);

        LiveTelemetryDTO result = liveTelemetryService.upsert(requestDTO);

        assertNotNull(result);
        assertEquals("TEST-DEVICE-001", result.getDeviceId());
        verify(liveTelemetryRepository).findByDeviceId("TEST-DEVICE-001");
        verify(liveTelemetryMapper).updateEntity(requestDTO, existingEntity);
        verify(liveTelemetryMapper, never()).toEntity(any());
        verify(liveTelemetryRepository).save(existingEntity);
        verify(liveTelemetryMapper).toDTO(existingEntity);
    }

    @Test
    void upsert_nullDeviceId() {
        requestDTO.setDeviceId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            liveTelemetryService.upsert(requestDTO);
        });

        assertEquals("deviceId is required", exception.getMessage());
        verify(liveTelemetryRepository, never()).findByDeviceId(anyString());
        verify(liveTelemetryRepository, never()).save(any());
    }

    @Test
    void upsert_blankDeviceId() {
        requestDTO.setDeviceId("   ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            liveTelemetryService.upsert(requestDTO);
        });

        assertEquals("deviceId is required", exception.getMessage());
        verify(liveTelemetryRepository, never()).findByDeviceId(anyString());
        verify(liveTelemetryRepository, never()).save(any());
    }

    @Test
    void getByDeviceId_success() {
        when(liveTelemetryRepository.findByDeviceId("TEST-DEVICE-001")).thenReturn(Optional.of(existingEntity));
        when(liveTelemetryMapper.toDTO(existingEntity)).thenReturn(liveTelemetryDTO);

        Optional<LiveTelemetryDTO> result = liveTelemetryService.getByDeviceId("TEST-DEVICE-001");

        assertTrue(result.isPresent());
        assertEquals("TEST-DEVICE-001", result.get().getDeviceId());
        verify(liveTelemetryRepository).findByDeviceId("TEST-DEVICE-001");
        verify(liveTelemetryMapper).toDTO(existingEntity);
    }

    @Test
    void getByDeviceId_notFound() {
        when(liveTelemetryRepository.findByDeviceId("NONEXISTENT")).thenReturn(Optional.empty());

        Optional<LiveTelemetryDTO> result = liveTelemetryService.getByDeviceId("NONEXISTENT");

        assertTrue(result.isEmpty());
        verify(liveTelemetryRepository).findByDeviceId("NONEXISTENT");
        verify(liveTelemetryMapper, never()).toDTO(any());
    }

    @Test
    void getByDeviceId_nullDeviceId() {
        Optional<LiveTelemetryDTO> result = liveTelemetryService.getByDeviceId(null);

        assertTrue(result.isEmpty());
        verify(liveTelemetryRepository, never()).findByDeviceId(anyString());
    }

    @Test
    void getByDeviceId_blankDeviceId() {
        Optional<LiveTelemetryDTO> result = liveTelemetryService.getByDeviceId("   ");

        assertTrue(result.isEmpty());
        verify(liveTelemetryRepository, never()).findByDeviceId(anyString());
    }

    @Test
    void cleanupStaleEntries_success() {
        Instant cutoff = Instant.now().minusSeconds(3600);
        
        doNothing().when(liveTelemetryRepository).deleteOlderThan(cutoff);

        assertDoesNotThrow(() -> liveTelemetryService.cleanupStaleEntries(cutoff));

        verify(liveTelemetryRepository).deleteOlderThan(cutoff);
    }
}
