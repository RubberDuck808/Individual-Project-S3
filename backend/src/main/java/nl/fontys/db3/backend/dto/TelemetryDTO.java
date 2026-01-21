package nl.fontys.db3.backend.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class TelemetryDTO extends BaseTelemetryFields {
    // All fields inherited from BaseTelemetryFields:
    // - id, deviceId, timestamp
    // - speedKph, rpm, throttlePct
    // - coolantTempC, batteryVoltageV, oilTempC, fuelLevelPct
    // - intakeAirTempC, engineLoadPct, mafAirFlow, mapPressure, timingAdvance
    // - diagnosticCodes
}
