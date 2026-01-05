package nl.fontys.db3.backend.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TripCompleteRequestDTO {
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
    private Double distanceKm;
    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;
}
