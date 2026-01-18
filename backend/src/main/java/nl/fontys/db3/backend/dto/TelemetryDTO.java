package nl.fontys.db3.backend.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryDTO {
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
