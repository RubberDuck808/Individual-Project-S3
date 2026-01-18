package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Live telemetry data - one row per device/user
 * Always updated (upsert) for real-time display on map
 * Contains only essential real-time data for performance
 */
@Entity
@Table(name = "live_telemetry", uniqueConstraints = {
    @UniqueConstraint(columnNames = "device_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    // Essential real-time data for map display
    @Column(name = "speed_kph")
    private Double speedKph;
    
    @Column(name = "rpm")
    private Double rpm;
    
    // Optional: location if ESP32 has GPS
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
}
