package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.LiveTelemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LiveTelemetryRepository extends JpaRepository<LiveTelemetry, Long> {

    Optional<LiveTelemetry> findByDeviceId(String deviceId);

    @Modifying
    @Query("DELETE FROM LiveTelemetry l WHERE l.lastUpdated < :cutoff")
    void deleteOlderThan(@Param("cutoff") java.time.Instant cutoff);
}
