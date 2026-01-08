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
    private Double speedKph;
    private Double rpm;
    private Double throttlePct;
}
