package nl.fontys.db3.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TelemetryRequestDTO extends BaseTelemetryRequestFields {
    @JsonProperty("deviceId")
    private String deviceId;
    
    // All fields inherited from BaseTelemetryRequestFields:
    // - speedKph, rpm, throttlePct
    // - coolantTempC, batteryVoltageV, oilTempC, fuelLevelPct
    // - intakeAirTempC, engineLoadPct, mafAirFlow, mapPressure, timingAdvance
    // - diagnosticCodes
}
