package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.DeviceOwnership;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.DeviceOwnershipRepository;
import nl.fontys.db3.backend.repository.DeviceRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceOwnershipService {

    private final DeviceOwnershipRepository ownershipRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceOwnershipService(DeviceOwnershipRepository ownershipRepository,
                                  DeviceRepository deviceRepository,
                                  UserRepository userRepository) {
        this.ownershipRepository = ownershipRepository;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Assign device to a user (creates ownership record)
     * If device already has an active owner, transfers ownership
     * Enforces maximum 1 device per user
     */
    @Transactional
    public DeviceOwnership assignDeviceToUser(String deviceId, Long userId, String notes) {
        deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<DeviceOwnership> userDevices = ownershipRepository.findByUser_IdAndActiveTrue(userId);
        if (!userDevices.isEmpty()) {
            boolean isSameDevice = userDevices.stream()
                    .anyMatch(ownership -> ownership.getDeviceId().equals(deviceId));
            
            if (!isSameDevice) {
                throw new IllegalArgumentException("User already has a device. Maximum 1 device per user. Please remove your current device first.");
            }
        }

        Optional<DeviceOwnership> existing = ownershipRepository.findByDeviceIdAndActiveTrue(deviceId);
        if (existing.isPresent()) {
            DeviceOwnership oldOwnership = existing.get();
            oldOwnership.setActive(false);
            oldOwnership.setTransferredAt(LocalDateTime.now());
            ownershipRepository.save(oldOwnership);
        }

        DeviceOwnership ownership = DeviceOwnership.builder()
                .deviceId(deviceId)
                .user(user)
                .active(true)
                .createdAt(LocalDateTime.now())
                .notes(notes)
                .build();

        return ownershipRepository.save(ownership);
    }

    /**
     * Unassign device from user (makes device unowned)
     */
    @Transactional
    public void unassignDevice(String deviceId, String notes) {
        Optional<DeviceOwnership> existing = ownershipRepository.findByDeviceIdAndActiveTrue(deviceId);
        if (existing.isPresent()) {
            DeviceOwnership ownership = existing.get();
            ownership.setActive(false);
            ownership.setTransferredAt(LocalDateTime.now());
            ownership.setNotes(notes);
            ownershipRepository.save(ownership);
        }
    }

    /**
     * Get current owner of a device
     */
    public Optional<User> getCurrentOwner(String deviceId) {
        return ownershipRepository.findByDeviceIdAndActiveTrue(deviceId)
                .map(DeviceOwnership::getUser);
    }

    /**
     * Get all devices owned by a user
     */
    public List<DeviceOwnership> getDevicesByUser(Long userId) {
        return ownershipRepository.findByUser_IdAndActiveTrue(userId);
    }

    /**
     * Get ownership history for a device
     */
    public List<DeviceOwnership> getOwnershipHistory(String deviceId) {
        return ownershipRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    /**
     * Check if user owns a device
     */
    public boolean isOwner(String deviceId, Long userId) {
        return ownershipRepository.findByDeviceIdAndActiveTrue(deviceId)
                .map(ownership -> ownership.getUser() != null && ownership.getUser().getId().equals(userId))
                .orElse(false);
    }
}
