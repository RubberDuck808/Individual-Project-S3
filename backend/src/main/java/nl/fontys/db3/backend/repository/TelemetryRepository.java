package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Telemetry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {

    // Latest telemetry across all devices
    List<Telemetry> findAllByOrderByTimestampDesc(Pageable pageable);

    // Latest telemetry for one device
    List<Telemetry> findByDeviceIdOrderByTimestampDesc(String deviceId, Pageable pageable);

    // Single latest row for one device
    Optional<Telemetry> findFirstByDeviceIdOrderByTimestampDesc(String deviceId);
}
