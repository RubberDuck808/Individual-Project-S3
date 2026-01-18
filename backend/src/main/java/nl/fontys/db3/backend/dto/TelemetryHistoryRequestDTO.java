package nl.fontys.db3.backend.dto;

import lombok.*;

/**
 * Request DTO for storing historical telemetry
 * ESP32 sends this for data logging and analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryHistoryRequestDTO {
    @jakarta.validation.constraints.NotBlank
    private String deviceId;
    
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
