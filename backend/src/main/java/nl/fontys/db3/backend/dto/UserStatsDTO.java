package nl.fontys.db3.backend.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserStatsDTO {
    private long totalTrips;
    private double totalDistanceKm;
    private long totalHazardsReported;
    private long totalVotes; // lifetime votes cast
}
