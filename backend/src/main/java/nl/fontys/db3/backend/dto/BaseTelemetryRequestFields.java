package nl.fontys.db3.backend.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Base class containing common telemetry request fields
 * Used to reduce code duplication between TelemetryRequestDTO and TelemetryHistoryRequestDTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseTelemetryRequestFields {
    // Note: deviceId is declared in child classes to allow different validation requirements
    
    // Basic engine data
    private Double speedKph;
    private Double rpm;
    private Double throttlePct;
    
    // Extended OBD data (optional)
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
