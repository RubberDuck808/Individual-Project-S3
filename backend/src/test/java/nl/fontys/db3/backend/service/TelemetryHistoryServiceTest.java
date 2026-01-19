package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.CarHealthDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryRequestDTO;
import nl.fontys.db3.backend.entity.TelemetryHistory;
import nl.fontys.db3.backend.mapper.TelemetryHistoryMapper;
import nl.fontys.db3.backend.repository.TelemetryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static nl.fontys.db3.backend.service.Constants.MAX_TELEMETRY_HISTORY_LIMIT;
import static nl.fontys.db3.backend.service.Constants.MIN_TELEMETRY_HISTORY_LIMIT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryHistoryServiceTest {

    @Mock
    private TelemetryHistoryRepository historyRepository;

    @Mock
    private TelemetryHistoryMapper historyMapper;

    @Mock
    private TelemetryWsPublisher wsPublisher;

    @InjectMocks
    private TelemetryHistoryService telemetryHistoryService;

    private TelemetryHistoryRequestDTO requestDTO;
    private TelemetryHistory entity;
    private TelemetryHistoryDTO historyDTO;
    private TelemetryHistory savedEntity;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();

        requestDTO = TelemetryHistoryRequestDTO.builder()
                .deviceId("TEST-DEVICE-001")
                .speedKph(60.0)
                .rpm(2500.0)
                .coolantTempC(90.0)
                .batteryVoltageV(12.5)
                .oilTempC(100.0)
                .fuelLevelPct(75.0)
                .diagnosticCodes("P0100,P0200")
                .build();

        entity = TelemetryHistory.builder()
                .deviceId("TEST-DEVICE-001")
                .timestamp(now)
                .speedKph(60.0)
                .rpm(2500.0)
                .coolantTempC(90.0)
                .batteryVoltageV(12.5)
                .oilTempC(100.0)
                .fuelLevelPct(75.0)
                .diagnosticCodes("P0100,P0200")
                .build();

        savedEntity = TelemetryHistory.builder()
                .id(1L)
                .deviceId("TEST-DEVICE-001")
                .timestamp(now)
                .speedKph(60.0)
                .rpm(2500.0)
                .coolantTempC(90.0)
                .batteryVoltageV(12.5)
                .oilTempC(100.0)
                .fuelLevelPct(75.0)
                .diagnosticCodes("P0100,P0200")
                .build();

        historyDTO = TelemetryHistoryDTO.builder()
                .id(1L)
                .deviceId("TEST-DEVICE-001")
                .timestamp(now)
                .speedKph(60.0)
                .rpm(2500.0)
                .coolantTempC(90.0)
                .batteryVoltageV(12.5)
                .oilTempC(100.0)
                .fuelLevelPct(75.0)
                .diagnosticCodes("P0100,P0200")
                .build();
    }

    @Test
    void store_success() {
        when(historyMapper.toEntity(requestDTO)).thenReturn(entity);
        when(historyRepository.save(entity)).thenReturn(savedEntity);
        when(historyMapper.toDTO(savedEntity)).thenReturn(historyDTO);
        doNothing().when(wsPublisher).update(anyString(), any(CarHealthDTO.class));

        TelemetryHistoryDTO result = telemetryHistoryService.store(requestDTO);

        assertNotNull(result);
        assertEquals("TEST-DEVICE-001", result.getDeviceId());
        verify(historyMapper).toEntity(requestDTO);
        verify(historyRepository).save(entity);
        verify(historyMapper).toDTO(savedEntity);
        verify(wsPublisher).update(eq("TEST-DEVICE-001"), any(CarHealthDTO.class));
    }

    @Test
    void store_nullDeviceId() {
        requestDTO.setDeviceId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            telemetryHistoryService.store(requestDTO);
        });

        assertEquals("deviceId is required", exception.getMessage());
        verify(historyMapper, never()).toEntity(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void store_blankDeviceId() {
        requestDTO.setDeviceId("   ");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            telemetryHistoryService.store(requestDTO);
        });

        assertEquals("deviceId is required", exception.getMessage());
        verify(historyMapper, never()).toEntity(any());
        verify(historyRepository, never()).save(any());
    }

    @Test
    void getLatest_success() {
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class)))
                .thenReturn(List.of(savedEntity));
        when(historyMapper.toDTO(savedEntity)).thenReturn(historyDTO);

        List<TelemetryHistoryDTO> result = telemetryHistoryService.getLatest("TEST-DEVICE-001", 50);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TEST-DEVICE-001", result.get(0).getDeviceId());
        verify(historyRepository).findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class));
    }

    @Test
    void getLatest_nullDeviceId() {
        List<TelemetryHistoryDTO> result = telemetryHistoryService.getLatest(null, 50);

        assertTrue(result.isEmpty());
        verify(historyRepository, never()).findByDeviceIdOrderByTimestampDesc(anyString(), any());
    }

    @Test
    void getLatest_blankDeviceId() {
        List<TelemetryHistoryDTO> result = telemetryHistoryService.getLatest("   ", 50);

        assertTrue(result.isEmpty());
        verify(historyRepository, never()).findByDeviceIdOrderByTimestampDesc(anyString(), any());
    }

    @Test
    void getLatest_limitTooLow() {
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        List<TelemetryHistoryDTO> result = telemetryHistoryService.getLatest("TEST-DEVICE-001", 0);

        assertNotNull(result);
        // Should clamp to MIN_TELEMETRY_HISTORY_LIMIT
        verify(historyRepository).findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), 
                argThat(pageRequest -> pageRequest.getPageSize() == MIN_TELEMETRY_HISTORY_LIMIT));
    }

    @Test
    void getLatest_limitTooHigh() {
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        List<TelemetryHistoryDTO> result = telemetryHistoryService.getLatest("TEST-DEVICE-001", 1000);

        assertNotNull(result);
        // Should clamp to MAX_TELEMETRY_HISTORY_LIMIT
        verify(historyRepository).findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"),
                argThat(pageRequest -> pageRequest.getPageSize() == MAX_TELEMETRY_HISTORY_LIMIT));
    }

    @Test
    void getHistory_success() {
        Instant start = now.minusSeconds(3600);
        Instant end = now;
        
        when(historyRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), eq(start), eq(end), any(PageRequest.class)))
                .thenReturn(List.of(savedEntity));
        when(historyMapper.toDTO(savedEntity)).thenReturn(historyDTO);

        List<TelemetryHistoryDTO> result = telemetryHistoryService.getHistory("TEST-DEVICE-001", start, end, 100);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(historyRepository).findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), eq(start), eq(end), any(PageRequest.class));
    }

    @Test
    void getHistory_nullDeviceId() {
        Instant start = now.minusSeconds(3600);
        Instant end = now;

        List<TelemetryHistoryDTO> result = telemetryHistoryService.getHistory(null, start, end, 100);

        assertTrue(result.isEmpty());
        verify(historyRepository, never()).findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                anyString(), any(), any(), any());
    }

    @Test
    void getHistory_blankDeviceId() {
        Instant start = now.minusSeconds(3600);
        Instant end = now;

        List<TelemetryHistoryDTO> result = telemetryHistoryService.getHistory("   ", start, end, 100);

        assertTrue(result.isEmpty());
        verify(historyRepository, never()).findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                anyString(), any(), any(), any());
    }

    @Test
    void getHistory_limitClamping() {
        Instant start = now.minusSeconds(3600);
        Instant end = now;

        when(historyRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                anyString(), any(), any(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // Test limit too low
        telemetryHistoryService.getHistory("TEST-DEVICE-001", start, end, 0);
        verify(historyRepository).findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                anyString(), any(), any(),
                argThat(pageRequest -> pageRequest.getPageSize() == 1));

        // Test limit too high
        telemetryHistoryService.getHistory("TEST-DEVICE-001", start, end, 2000);
        verify(historyRepository, atLeastOnce()).findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                anyString(), any(), any(),
                argThat(pageRequest -> pageRequest.getPageSize() == 1000));
    }

    @Test
    void getLatestCarHealth_success() {
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class)))
                .thenReturn(List.of(savedEntity));

        Optional<CarHealthDTO> result = telemetryHistoryService.getLatestCarHealth("TEST-DEVICE-001");

        assertTrue(result.isPresent());
        assertTrue(result.get().getConnected());
        assertEquals(60.0, result.get().getSpeedKmh());
        assertEquals(2500.0, result.get().getRpm());
        assertEquals(90.0, result.get().getCoolantC());
        assertEquals(12.5, result.get().getBatteryV());
        assertEquals(100.0, result.get().getOilTempC());
        assertEquals(75.0, result.get().getFuelPct());
        assertEquals(2, result.get().getErrorCodes().size());
        assertEquals(100, result.get().getErrorCodes().get(0));
        assertEquals(200, result.get().getErrorCodes().get(1));
    }

    @Test
    void getLatestCarHealth_nullDeviceId() {
        Optional<CarHealthDTO> result = telemetryHistoryService.getLatestCarHealth(null);

        assertTrue(result.isEmpty());
        verify(historyRepository, never()).findByDeviceIdOrderByTimestampDesc(anyString(), any());
    }

    @Test
    void getLatestCarHealth_blankDeviceId() {
        Optional<CarHealthDTO> result = telemetryHistoryService.getLatestCarHealth("   ");

        assertTrue(result.isEmpty());
        verify(historyRepository, never()).findByDeviceIdOrderByTimestampDesc(anyString(), any());
    }

    @Test
    void getLatestCarHealth_notFound() {
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                eq("NONEXISTENT"), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        Optional<CarHealthDTO> result = telemetryHistoryService.getLatestCarHealth("NONEXISTENT");

        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestCarHealth_diagnosticCodesWithPattern() {
        savedEntity.setDiagnosticCodes("P0100,B0200");
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class)))
                .thenReturn(List.of(savedEntity));

        Optional<CarHealthDTO> result = telemetryHistoryService.getLatestCarHealth("TEST-DEVICE-001");

        assertTrue(result.isPresent());
        assertEquals(2, result.get().getErrorCodes().size());
        // P0100 should be parsed as 100 (after prefix)
        assertEquals(100, result.get().getErrorCodes().get(0));
        // B0200 should be parsed as 200
        assertEquals(200, result.get().getErrorCodes().get(1));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "INVALID,NOTANUMBER"})
    void getLatestCarHealth_diagnosticCodesEmptyOrInvalid(String diagnosticCodes) {
        savedEntity.setDiagnosticCodes(diagnosticCodes);
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class)))
                .thenReturn(List.of(savedEntity));

        Optional<CarHealthDTO> result = telemetryHistoryService.getLatestCarHealth("TEST-DEVICE-001");

        assertTrue(result.isPresent());
        assertTrue(result.get().getErrorCodes().isEmpty());
    }

    @Test
    void getCarHealthHistory_success() {
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class)))
                .thenReturn(List.of(savedEntity));

        List<CarHealthDTO> result = telemetryHistoryService.getCarHealthHistory("TEST-DEVICE-001", 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getConnected());
        verify(historyRepository).findByDeviceIdOrderByTimestampDesc(
                eq("TEST-DEVICE-001"), any(PageRequest.class));
    }

    @Test
    void getCarHealthHistory_nullDeviceId() {
        List<CarHealthDTO> result = telemetryHistoryService.getCarHealthHistory(null, 10);

        assertTrue(result.isEmpty());
        verify(historyRepository, never()).findByDeviceIdOrderByTimestampDesc(anyString(), any());
    }

    @Test
    void getCarHealthHistory_blankDeviceId() {
        List<CarHealthDTO> result = telemetryHistoryService.getCarHealthHistory("   ", 10);

        assertTrue(result.isEmpty());
        verify(historyRepository, never()).findByDeviceIdOrderByTimestampDesc(anyString(), any());
    }

    @Test
    void getCarHealthHistory_limitClamping() {
        when(historyRepository.findByDeviceIdOrderByTimestampDesc(
                anyString(), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        // Test limit too low
        telemetryHistoryService.getCarHealthHistory("TEST-DEVICE-001", 0);
        verify(historyRepository).findByDeviceIdOrderByTimestampDesc(
                anyString(), argThat(pageRequest -> pageRequest.getPageSize() == 1));

        // Test limit too high
        telemetryHistoryService.getCarHealthHistory("TEST-DEVICE-001", 200);
        verify(historyRepository, atLeastOnce()).findByDeviceIdOrderByTimestampDesc(
                anyString(), argThat(pageRequest -> pageRequest.getPageSize() == 100));
    }

    @Test
    void cleanupOldHistory_success() {
        Instant cutoff = now.minusSeconds(86400); // 24 hours ago
        
        doNothing().when(historyRepository).deleteByDeviceIdAndTimestampBefore("TEST-DEVICE-001", cutoff);

        assertDoesNotThrow(() -> telemetryHistoryService.cleanupOldHistory("TEST-DEVICE-001", cutoff));

        verify(historyRepository).deleteByDeviceIdAndTimestampBefore("TEST-DEVICE-001", cutoff);
    }
}
