package nl.fontys.db3.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryRequestDTO {
    private String deviceId;
    private Double speedKph;
    private Double rpm;
    private Double throttlePct;
}
