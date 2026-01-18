package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDeviceDTO {
    private Long id;
    private String deviceId;
    private String description;
    private String deviceType;
    private String firmwareVersion;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;
    
    // Ownership info
    private Long currentOwnerId;
    private String currentOwnerUsername;
    
    // Performance metrics
    private Long totalTelemetryCount;
    private Instant lastTelemetryTimestamp;
    private Double lastSpeedKph;
    private Double lastRpm;
    private Long liveTelemetryId;
}
