package nl.fontys.db3.backend.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

/**
 * Base class containing common telemetry fields
 * Used to reduce code duplication between TelemetryDTO and TelemetryHistoryDTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseTelemetryFields {
    private Long id;
    private String deviceId;
    private Instant timestamp;
    
    // Basic engine data
    private Double speedKph;
    private Double rpm;
    private Double throttlePct;
    
    // Extended OBD data
    private Double coolantTempC;
    private Double batteryVoltageV;
    private Double oilTempC;
    private Double fuelLevelPct;
    private Double intakeAirTempC;
    private Double engineLoadPct;
    private Double mafAirFlow;
    private Double mapPressure;
    private Double timingAdvance;
    
    // Diagnostic codes
    private String diagnosticCodes;
}
