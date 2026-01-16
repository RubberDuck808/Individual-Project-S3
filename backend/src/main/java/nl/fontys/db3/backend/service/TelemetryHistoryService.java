package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.CarHealthDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryRequestDTO;
import nl.fontys.db3.backend.entity.TelemetryHistory;
import nl.fontys.db3.backend.mapper.TelemetryHistoryMapper;
import nl.fontys.db3.backend.repository.TelemetryHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static nl.fontys.db3.backend.service.Constants.MAX_TELEMETRY_HISTORY_LIMIT;
import static nl.fontys.db3.backend.service.Constants.MIN_TELEMETRY_HISTORY_LIMIT;
import static nl.fontys.db3.backend.service.Constants.DIAGNOSTIC_CODE_PATTERN;
import static nl.fontys.db3.backend.service.Constants.DIAGNOSTIC_CODE_PREFIX_LENGTH;

@Service
public class TelemetryHistoryService {

    private final TelemetryHistoryRepository historyRepository;
    private final TelemetryHistoryMapper historyMapper;
    private final TelemetryWsPublisher wsPublisher;

    public TelemetryHistoryService(TelemetryHistoryRepository historyRepository,
                                   TelemetryHistoryMapper historyMapper,
                                   TelemetryWsPublisher wsPublisher) {
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;
        this.wsPublisher = wsPublisher;
    }

    /**
     * Store a historical telemetry data point
     * Used for logging and analysis
     * Publishes WebSocket event for real-time updates
     */
    @Transactional
    public TelemetryHistoryDTO store(TelemetryHistoryRequestDTO dto) {
        if (dto.getDeviceId() == null || dto.getDeviceId().isBlank()) {
            throw new IllegalArgumentException("deviceId is required");
        }

        TelemetryHistory entity = historyMapper.toEntity(dto);
        TelemetryHistory saved = historyRepository.save(entity);
        TelemetryHistoryDTO savedDto = historyMapper.toDTO(saved);
        
        // Convert to CarHealthDTO and publish WebSocket event
        CarHealthDTO carHealth = toCarHealthDTO(saved);
        wsPublisher.update(dto.getDeviceId(), carHealth);
        
        return savedDto;
    }

    /**
     * Get latest historical data for a device
     */
    public List<TelemetryHistoryDTO> getLatest(String deviceId, int limit) {
        if (deviceId == null || deviceId.isBlank()) {
            return Collections.emptyList();
        }

        int safeLimit = Math.clamp(limit, MIN_TELEMETRY_HISTORY_LIMIT, MAX_TELEMETRY_HISTORY_LIMIT);

        return historyRepository
                .findByDeviceIdOrderByTimestampDesc(deviceId, PageRequest.of(0, safeLimit))
                .stream()
                .map(historyMapper::toDTO)
                .toList();
    }

    /**
     * Get history within time range
     */
    public List<TelemetryHistoryDTO> getHistory(String deviceId, Instant start, Instant end, int limit) {
        if (deviceId == null || deviceId.isBlank()) {
            return Collections.emptyList();
        }

        int safeLimit = Math.clamp(limit, 1, 1000);

        return historyRepository
                .findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                    deviceId, start, end, PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(historyMapper::toDTO)
                .toList();
    }

    /**
     * Get latest car health data from history (formatted for frontend)
     */
    public Optional<CarHealthDTO> getLatestCarHealth(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return Optional.empty();
        }

        return historyRepository
                .findByDeviceIdOrderByTimestampDesc(deviceId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(this::toCarHealthDTO);
    }

    /**
     * Get car health history
     */
    public List<CarHealthDTO> getCarHealthHistory(String deviceId, int limit) {
        if (deviceId == null || deviceId.isBlank()) {
            return Collections.emptyList();
        }

        int safeLimit = Math.clamp(limit, 1, 100);

        return historyRepository
                .findByDeviceIdOrderByTimestampDesc(deviceId, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toCarHealthDTO)
                .toList();
    }

    /**
     * Convert TelemetryHistory to CarHealthDTO
     */
    private CarHealthDTO toCarHealthDTO(TelemetryHistory history) {
        List<Integer> errorCodes = parseErrorCodes(history.getDiagnosticCodes());

        return CarHealthDTO.builder()
                .connected(true)
                .speedKmh(history.getSpeedKph())
                .rpm(history.getRpm())
                .coolantC(history.getCoolantTempC())
                .batteryV(history.getBatteryVoltageV())
                .oilTempC(history.getOilTempC())
                .fuelPct(history.getFuelLevelPct())
                .errorCodes(errorCodes)
                .build();
    }

    /**
     * Parse diagnostic codes string to list of integers
     */
    private List<Integer> parseErrorCodes(String diagnosticCodes) {
        if (diagnosticCodes == null || diagnosticCodes.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return Arrays.stream(diagnosticCodes.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(code -> {
                        if (code.matches(DIAGNOSTIC_CODE_PATTERN)) {
                            return Integer.parseInt(code.substring(DIAGNOSTIC_CODE_PREFIX_LENGTH));
                        }
                        return Integer.parseInt(code);
                    })
                    .toList();
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Cleanup old history data (retention policy)
     */
    @Transactional
    public void cleanupOldHistory(String deviceId, Instant cutoff) {
        historyRepository.deleteByDeviceIdAndTimestampBefore(deviceId, cutoff);
    }
}
