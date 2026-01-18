package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.CarHealthDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryRequestDTO;
import nl.fontys.db3.backend.entity.TelemetryHistory;
import nl.fontys.db3.backend.mapper.TelemetryHistoryMapper;
import nl.fontys.db3.backend.repository.TelemetryHistoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryHistoryServiceTest {

    @Mock private TelemetryHistoryRepository historyRepository;
    @Mock private TelemetryHistoryMapper historyMapper;
    @Mock private TelemetryWsPublisher wsPublisher;

    @InjectMocks
    private TelemetryHistoryService service;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void store_invalidDeviceId_throws(String deviceId) {
        TelemetryHistoryRequestDTO dto = mock(TelemetryHistoryRequestDTO.class);
        when(dto.getDeviceId()).thenReturn(deviceId);

        assertThrows(IllegalArgumentException.class, () -> service.store(dto));
        verifyNoInteractions(historyRepository, historyMapper, wsPublisher);
    }

    @Test
    void store_success_savesAndPublishes() {
        TelemetryHistoryRequestDTO dto = mock(TelemetryHistoryRequestDTO.class);
        when(dto.getDeviceId()).thenReturn("device1");

        TelemetryHistory entity = mock(TelemetryHistory.class);
        when(historyMapper.toEntity(dto)).thenReturn(entity);

        TelemetryHistory saved = mock(TelemetryHistory.class);
        when(historyRepository.save(entity)).thenReturn(saved);

        TelemetryHistoryDTO savedDto = mock(TelemetryHistoryDTO.class);
        when(historyMapper.toDTO(saved)).thenReturn(savedDto);

        when(saved.getDiagnosticCodes()).thenReturn(null);
        when(saved.getSpeedKph()).thenReturn(60.0);
        when(saved.getRpm()).thenReturn(2000.0);
        when(saved.getCoolantTempC()).thenReturn(90.0);
        when(saved.getBatteryVoltageV()).thenReturn(12.5);
        when(saved.getOilTempC()).thenReturn(85.0);
        when(saved.getFuelLevelPct()).thenReturn(75.0);

        TelemetryHistoryDTO result = service.store(dto);

        assertSame(savedDto, result);
        verify(historyRepository).save(entity);
        verify(wsPublisher).update(eq("device1"), any(CarHealthDTO.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getLatest_invalidDeviceId_returnsEmpty(String deviceId) {
        List<TelemetryHistoryDTO> result = service.getLatest(deviceId, 10);

        assertTrue(result.isEmpty());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void getLatest_success_returnsDTOs() {
        TelemetryHistory history = mock(TelemetryHistory.class);
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(eq("device1"), any()))
                .thenReturn(List.of(history));
        TelemetryHistoryDTO dto = mock(TelemetryHistoryDTO.class);
        when(historyMapper.toDTO(history)).thenReturn(dto);

        List<TelemetryHistoryDTO> result = service.getLatest("device1", 10);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getHistory_invalidDeviceId_returnsEmpty(String deviceId) {
        List<TelemetryHistoryDTO> result = service.getHistory(deviceId, Instant.now(), Instant.now(), 10);

        assertTrue(result.isEmpty());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void getHistory_success_returnsDTOs() {
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();
        TelemetryHistory history = mock(TelemetryHistory.class);
        when(historyRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                eq("device1"), eq(start), eq(end), any()))
                .thenReturn(List.of(history));
        TelemetryHistoryDTO dto = mock(TelemetryHistoryDTO.class);
        when(historyMapper.toDTO(history)).thenReturn(dto);

        List<TelemetryHistoryDTO> result = service.getHistory("device1", start, end, 10);

        assertEquals(1, result.size());
        assertSame(dto, result.get(0));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getLatestCarHealth_invalidDeviceId_returnsEmpty(String deviceId) {
        Optional<CarHealthDTO> result = service.getLatestCarHealth(deviceId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void getLatestCarHealth_success_returnsCarHealth() {
        TelemetryHistory history = mock(TelemetryHistory.class);
        when(history.getDiagnosticCodes()).thenReturn("P0123,P0456");
        when(history.getSpeedKph()).thenReturn(60.0);
        when(history.getRpm()).thenReturn(2000.0);
        when(history.getCoolantTempC()).thenReturn(90.0);
        when(history.getBatteryVoltageV()).thenReturn(12.5);
        when(history.getOilTempC()).thenReturn(85.0);
        when(history.getFuelLevelPct()).thenReturn(75.0);
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(eq("device1"), any()))
                .thenReturn(List.of(history));

        Optional<CarHealthDTO> result = service.getLatestCarHealth("device1");

        assertTrue(result.isPresent());
        CarHealthDTO carHealth = result.get();
        assertTrue(carHealth.getConnected());
        assertEquals(60.0, carHealth.getSpeedKmh());
        assertEquals(2000.0, carHealth.getRpm());
        assertNotNull(carHealth.getErrorCodes());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getCarHealthHistory_invalidDeviceId_returnsEmpty(String deviceId) {
        List<CarHealthDTO> result = service.getCarHealthHistory(deviceId, 10);

        assertTrue(result.isEmpty());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void getCarHealthHistory_success_returnsCarHealthList() {
        TelemetryHistory history = mock(TelemetryHistory.class);
        when(history.getDiagnosticCodes()).thenReturn(null);
        when(history.getSpeedKph()).thenReturn(60.0);
        when(history.getRpm()).thenReturn(2000.0);
        when(history.getCoolantTempC()).thenReturn(90.0);
        when(history.getBatteryVoltageV()).thenReturn(12.5);
        when(history.getOilTempC()).thenReturn(85.0);
        when(history.getFuelLevelPct()).thenReturn(75.0);
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(eq("device1"), any()))
                .thenReturn(List.of(history));

        List<CarHealthDTO> result = service.getCarHealthHistory("device1", 10);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getConnected());
    }

}