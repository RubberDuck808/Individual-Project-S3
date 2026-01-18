package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.DeviceOwnership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceOwnershipRepository extends JpaRepository<DeviceOwnership, Long> {

    /**
     * Find the current active ownership for a device
     */
    Optional<DeviceOwnership> findByDeviceIdAndActiveTrue(String deviceId);

    /**
     * Find all ownership records for a device (including historical)
     */
    List<DeviceOwnership> findByDeviceIdOrderByCreatedAtDesc(String deviceId);

    /**
     * Find all devices owned by a user
     */
    List<DeviceOwnership> findByUser_IdAndActiveTrue(Long userId);

    /**
     * Find all ownership records for a user (including historical)
     */
    List<DeviceOwnership> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
