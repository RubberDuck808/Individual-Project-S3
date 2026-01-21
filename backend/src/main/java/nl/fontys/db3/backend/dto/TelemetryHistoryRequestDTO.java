package nl.fontys.db3.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Request DTO for storing historical telemetry
 * ESP32 sends this for data logging and analysis
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TelemetryHistoryRequestDTO extends BaseTelemetryRequestFields {
    @NotBlank
    @JsonProperty("deviceId")
    private String deviceId;

    // All fields inherited from BaseTelemetryRequestFields:
    // - speedKph, rpm, throttlePct
    // - coolantTempC, batteryVoltageV, oilTempC, fuelLevelPct
    // - intakeAirTempC, engineLoadPct, mafAirFlow, mapPressure, timingAdvance
    // - diagnosticCodes
    // Note: deviceId is overridden here with @NotBlank validation
}
