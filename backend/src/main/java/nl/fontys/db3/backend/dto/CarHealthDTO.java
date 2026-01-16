package nl.fontys.db3.backend.dto;

import lombok.*;
import java.util.List;

/**
 * DTO for car health data formatted for frontend display
 * Matches the structure expected by CarHealthPanel component
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarHealthDTO {
    private Boolean connected;
    private Double speedKmh;
    private Double rpm;
    private Double coolantC;
    private Double batteryV;
    private Double oilTempC;
    private Double fuelPct;
    private List<Integer> errorCodes; // Parsed from diagnosticCodes string
}
