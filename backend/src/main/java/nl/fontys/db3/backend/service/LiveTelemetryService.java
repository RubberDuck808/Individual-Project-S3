package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.LiveTelemetryDTO;
import nl.fontys.db3.backend.dto.LiveTelemetryRequestDTO;
import nl.fontys.db3.backend.entity.LiveTelemetry;
import nl.fontys.db3.backend.mapper.LiveTelemetryMapper;
import nl.fontys.db3.backend.repository.LiveTelemetryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LiveTelemetryService {

    private final LiveTelemetryRepository liveTelemetryRepository;
    private final LiveTelemetryMapper liveTelemetryMapper;

    public LiveTelemetryService(LiveTelemetryRepository liveTelemetryRepository,
                                LiveTelemetryMapper liveTelemetryMapper) {
        this.liveTelemetryRepository = liveTelemetryRepository;
        this.liveTelemetryMapper = liveTelemetryMapper;
    }

    /**
     * Upsert live telemetry - always updates the same row for a device
     * Used for real-time map display
     */
    @Transactional
    public LiveTelemetryDTO upsert(LiveTelemetryRequestDTO dto) {
        if (dto.getDeviceId() == null || dto.getDeviceId().isBlank()) {
            throw new IllegalArgumentException("deviceId is required");
        }

        Optional<LiveTelemetry> existing = liveTelemetryRepository.findByDeviceId(dto.getDeviceId());

        LiveTelemetry entity;
        if (existing.isPresent()) {
            // Update existing row
            entity = existing.get();
            liveTelemetryMapper.updateEntity(dto, entity);
        } else {
            entity = liveTelemetryMapper.toEntity(dto);
        }

        LiveTelemetry saved = liveTelemetryRepository.save(entity);
        return liveTelemetryMapper.toDTO(saved);
    }

    /**
     * Get latest live telemetry for a device
     */
    public Optional<LiveTelemetryDTO> getByDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return Optional.empty();
        }

        return liveTelemetryRepository
                .findByDeviceId(deviceId)
                .map(liveTelemetryMapper::toDTO);
    }

    /**
     * Cleanup old live telemetry entries (devices that haven't updated in X time)
     * Call this periodically to remove stale entries
     */
    @Transactional
    public void cleanupStaleEntries(java.time.Instant cutoff) {
        liveTelemetryRepository.deleteOlderThan(cutoff);
    }
}
