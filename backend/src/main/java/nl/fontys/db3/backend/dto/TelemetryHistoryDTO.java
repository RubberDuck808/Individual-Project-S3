package nl.fontys.db3.backend.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DTO for historical telemetry data
 */
@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class TelemetryHistoryDTO extends BaseTelemetryFields {
    // All fields inherited from BaseTelemetryFields:
    // - id, deviceId, timestamp
    // - speedKph, rpm, throttlePct
    // - coolantTempC, batteryVoltageV, oilTempC, fuelLevelPct
    // - intakeAirTempC, engineLoadPct, mafAirFlow, mapPressure, timingAdvance
    // - diagnosticCodes
}
