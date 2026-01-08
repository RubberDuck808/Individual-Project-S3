package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.TelemetryDTO;
import nl.fontys.db3.backend.dto.TelemetryRequestDTO;
import nl.fontys.db3.backend.entity.Telemetry;
import nl.fontys.db3.backend.mapper.TelemetryMapper;
import nl.fontys.db3.backend.repository.TelemetryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TelemetryService {

    private final TelemetryRepository telemetryRepository;
    private final TelemetryMapper telemetryMapper;

    public TelemetryService(TelemetryRepository telemetryRepository,
                            TelemetryMapper telemetryMapper) {
        this.telemetryRepository = telemetryRepository;
        this.telemetryMapper = telemetryMapper;
    }

    public TelemetryDTO ingest(TelemetryRequestDTO dto) {
        if (dto.getDeviceId() == null || dto.getDeviceId().isBlank()) {
            throw new IllegalArgumentException("deviceId is required");
        }

        Telemetry entity = telemetryMapper.toEntity(dto);
        Telemetry saved = telemetryRepository.save(entity);
        return telemetryMapper.toDTO(saved);
    }

    public List<TelemetryDTO> getLatest(int limit) {
        int safeLimit = Math.clamp(limit, 1, 500);

        return telemetryRepository
                .findAllByOrderByTimestampDesc(PageRequest.of(0, safeLimit))
                .stream()
                .map(telemetryMapper::toDTO)
                .toList();
    }

    public List<TelemetryDTO> getLatestForDevice(String deviceId, int limit) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId is required");
        }

        int safeLimit = Math.clamp(limit, 1, 500);

        return telemetryRepository
                .findByDeviceIdOrderByTimestampDesc(deviceId, PageRequest.of(0, safeLimit))
                .stream()
                .map(telemetryMapper::toDTO)
                .toList();
    }
}
