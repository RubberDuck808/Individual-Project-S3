package nl.fontys.db3.backend.service;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.AdminDeviceDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static nl.fontys.db3.backend.service.Constants.DEVICE_NOT_FOUND_PREFIX;

@Service
@RequiredArgsConstructor
public class AdminDeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceOwnershipRepository ownershipRepository;
    private final LiveTelemetryRepository liveTelemetryRepository;
    private final TelemetryHistoryRepository telemetryHistoryRepository;

    @Transactional(readOnly = true)
    public Page<AdminDeviceDTO> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable)
                .map(this::toAdminDeviceDTO);
    }

    @Transactional(readOnly = true)
    public AdminDeviceDTO getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(DEVICE_NOT_FOUND_PREFIX + id));
        return toAdminDeviceDTO(device);
    }

    @Transactional(readOnly = true)
    public AdminDeviceDTO getDeviceByDeviceId(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new RuntimeException(DEVICE_NOT_FOUND_PREFIX + deviceId));
        return toAdminDeviceDTO(device);
    }

    @Transactional
    public void deactivateDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(DEVICE_NOT_FOUND_PREFIX + id));
        device.setActive(false);
        deviceRepository.save(device);
    }

    @Transactional
    public void activateDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(DEVICE_NOT_FOUND_PREFIX + id));
        device.setActive(true);
        deviceRepository.save(device);
    }

    @Transactional
    public void updateDeviceDescription(Long id, String description) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(DEVICE_NOT_FOUND_PREFIX + id));
        device.setDescription(description);
        deviceRepository.save(device);
    }

    private AdminDeviceDTO toAdminDeviceDTO(Device device) {
        Optional<DeviceOwnership> ownership = ownershipRepository.findByDeviceIdAndActiveTrue(device.getDeviceId());
        Long currentOwnerId = null;
        String currentOwnerUsername = null;
        if (ownership.isPresent() && ownership.get().getUser() != null) {
            currentOwnerId = ownership.get().getUser().getId();
            currentOwnerUsername = ownership.get().getUser().getUsername();
        }

        Optional<LiveTelemetry> liveTelemetry = liveTelemetryRepository.findByDeviceId(device.getDeviceId());
        Long liveTelemetryId = liveTelemetry.map(LiveTelemetry::getId).orElse(null);
        Double lastSpeedKph = liveTelemetry.map(LiveTelemetry::getSpeedKph).orElse(null);
        Double lastRpm = liveTelemetry.map(LiveTelemetry::getRpm).orElse(null);

        long totalTelemetryCount = telemetryHistoryRepository.countByDeviceId(device.getDeviceId());
        
        List<TelemetryHistory> latestHistory = telemetryHistoryRepository.findByDeviceIdOrderByTimestampDesc(
                device.getDeviceId(),
                org.springframework.data.domain.PageRequest.of(0, 1)
        );
        Instant lastTelemetryTimestamp = latestHistory.isEmpty() 
                ? null 
                : latestHistory.get(0).getTimestamp();

        return AdminDeviceDTO.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .description(device.getDescription())
                .deviceType(device.getDeviceType())
                .firmwareVersion(device.getFirmwareVersion())
                .active(device.isActive())
                .createdAt(device.getCreatedAt())
                .lastSeenAt(device.getLastSeenAt())
                .currentOwnerId(currentOwnerId)
                .currentOwnerUsername(currentOwnerUsername)
                .totalTelemetryCount(totalTelemetryCount)
                .lastTelemetryTimestamp(lastTelemetryTimestamp)
                .lastSpeedKph(lastSpeedKph)
                .lastRpm(lastRpm)
                .liveTelemetryId(liveTelemetryId)
                .build();
    }
}
