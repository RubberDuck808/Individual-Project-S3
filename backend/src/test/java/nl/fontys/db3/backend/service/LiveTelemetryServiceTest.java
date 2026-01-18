package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.LiveTelemetryDTO;
import nl.fontys.db3.backend.dto.LiveTelemetryRequestDTO;
import nl.fontys.db3.backend.entity.LiveTelemetry;
import nl.fontys.db3.backend.mapper.LiveTelemetryMapper;
import nl.fontys.db3.backend.repository.LiveTelemetryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveTelemetryServiceTest {

    @Mock private LiveTelemetryRepository liveTelemetryRepository;
    @Mock private LiveTelemetryMapper liveTelemetryMapper;

    @InjectMocks
    private LiveTelemetryService service;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void upsert_invalidDeviceId_throws(String deviceId) {
        LiveTelemetryRequestDTO dto = mock(LiveTelemetryRequestDTO.class);
        when(dto.getDeviceId()).thenReturn(deviceId);

        assertThrows(IllegalArgumentException.class, () -> service.upsert(dto));

        verifyNoInteractions(liveTelemetryRepository, liveTelemetryMapper);
    }

    @Test
    void upsert_existingDevice_updatesEntity() {
        LiveTelemetryRequestDTO dto = mock(LiveTelemetryRequestDTO.class);
        when(dto.getDeviceId()).thenReturn("device1");

        LiveTelemetry existing = mock(LiveTelemetry.class);
        when(liveTelemetryRepository.findByDeviceId("device1"))
                .thenReturn(Optional.of(existing));

        LiveTelemetry saved = mock(LiveTelemetry.class);
        when(liveTelemetryRepository.save(existing)).thenReturn(saved);

        LiveTelemetryDTO dtoResult = mock(LiveTelemetryDTO.class);
        when(liveTelemetryMapper.toDTO(saved)).thenReturn(dtoResult);

        LiveTelemetryDTO result = service.upsert(dto);

        assertSame(dtoResult, result);
        verify(liveTelemetryMapper).updateEntity(dto, existing);
        verify(liveTelemetryRepository).save(existing);
    }

    @Test
    void upsert_newDevice_createsEntity() {
        LiveTelemetryRequestDTO dto = mock(LiveTelemetryRequestDTO.class);
        when(dto.getDeviceId()).thenReturn("device1");

        when(liveTelemetryRepository.findByDeviceId("device1"))
                .thenReturn(Optional.empty());

        LiveTelemetry newEntity = mock(LiveTelemetry.class);
        when(liveTelemetryMapper.toEntity(dto)).thenReturn(newEntity);

        LiveTelemetry saved = mock(LiveTelemetry.class);
        when(liveTelemetryRepository.save(newEntity)).thenReturn(saved);

        LiveTelemetryDTO dtoResult = mock(LiveTelemetryDTO.class);
        when(liveTelemetryMapper.toDTO(saved)).thenReturn(dtoResult);

        LiveTelemetryDTO result = service.upsert(dto);

        assertSame(dtoResult, result);
        verify(liveTelemetryMapper).toEntity(dto);
        verify(liveTelemetryRepository).save(newEntity);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getByDeviceId_invalidDeviceId_returnsEmpty(String deviceId) {
        Optional<LiveTelemetryDTO> result = service.getByDeviceId(deviceId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(liveTelemetryRepository);
    }

    @Test
    void getByDeviceId_success_returnsDTO() {
        LiveTelemetry entity = mock(LiveTelemetry.class);
        when(liveTelemetryRepository.findByDeviceId("device1"))
                .thenReturn(Optional.of(entity));

        LiveTelemetryDTO dto = mock(LiveTelemetryDTO.class);
        when(liveTelemetryMapper.toDTO(entity)).thenReturn(dto);

        Optional<LiveTelemetryDTO> result = service.getByDeviceId("device1");

        assertTrue(result.isPresent());
        assertSame(dto, result.get());
    }

}