package nl.fontys.db3.backend.dto;

import lombok.*;

/**
 * Request DTO for updating live telemetry
 * ESP32 sends this for real-time map updates
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveTelemetryRequestDTO {
    @jakarta.validation.constraints.NotBlank
    private String deviceId;
    
    private Double speedKph;
    private Double rpm;
    private Double latitude;
    private Double longitude;
}
