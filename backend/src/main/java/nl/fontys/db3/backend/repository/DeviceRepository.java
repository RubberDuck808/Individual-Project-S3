package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceId(String deviceId);

    Optional<Device> findByApiKeyHash(String apiKeyHash);

    Optional<Device> findByDeviceIdAndActiveTrue(String deviceId);

    Optional<Device> findByApiKeyHashAndActiveTrue(String apiKeyHash);

    long countByLastSeenAtAfter(LocalDateTime threshold);
}
