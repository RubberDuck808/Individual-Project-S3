package nl.fontys.db3.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryRequestDTO {
    private String deviceId;
    
    // Basic engine data
    private Double speedKph;
    private Double rpm;
    private Double throttlePct;
    
    // Extended OBD data (optional - ESP32 can send what it has)
    private Double coolantTempC;
    private Double batteryVoltageV;
    private Double oilTempC;
    private Double fuelLevelPct;
    private Double intakeAirTempC;
    private Double engineLoadPct;
    private Double mafAirFlow;
    private Double mapPressure;
    private Double timingAdvance;
    
    // Diagnostic codes (comma-separated, e.g., "P0301,P0420")
    private String diagnosticCodes;
}
