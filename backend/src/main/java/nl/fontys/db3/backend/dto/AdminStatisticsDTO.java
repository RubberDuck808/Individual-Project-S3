package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStatisticsDTO {
    // User statistics
    private Long totalUsers;
    private Long activeUsers; // Users active in last 30 days
    private Long adminUsers;
    
    // Hazard statistics
    private Long totalHazards;
    private Long openHazards;
    private Long verifiedHazards;
    private Long resolvedHazards;
    
    // Device statistics
    private Long totalDevices;
    private Long activeDevices; // Devices seen in last 24 hours
    private Long inactiveDevices;
    
    // Telemetry statistics
    private Long totalTelemetryRecords;
    private Instant lastTelemetryTimestamp;
    private Long devicesWithTelemetry;
    
    // Trip statistics
    private Long totalTrips;
    private Double totalDistanceKm;
    
    // Asset statistics
    private Long totalAvatars;
    private Long activeAvatars;
    private Long totalBackgrounds;
    private Long activeBackgrounds;
    
    // Timestamps
    private LocalDateTime lastUpdated;
}
