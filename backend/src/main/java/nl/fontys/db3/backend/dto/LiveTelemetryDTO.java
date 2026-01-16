package nl.fontys.db3.backend.dto;

import lombok.*;
import java.time.Instant;

/**
 * DTO for live telemetry data (real-time map display)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveTelemetryDTO {
    private String deviceId;
    private Instant lastUpdated;
    private Double speedKph;
    private Double rpm;
    private Double latitude;
    private Double longitude;
}
