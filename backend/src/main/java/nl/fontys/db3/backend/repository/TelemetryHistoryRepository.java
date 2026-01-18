package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.TelemetryHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TelemetryHistoryRepository extends JpaRepository<TelemetryHistory, Long> {

    List<TelemetryHistory> findByDeviceIdOrderByTimestampDesc(String deviceId, Pageable pageable);

    List<TelemetryHistory> findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
        String deviceId, Instant start, Instant end, Pageable pageable
    );

    void deleteByDeviceIdAndTimestampBefore(String deviceId, Instant cutoff);
    
    long countByDeviceId(String deviceId);
}
