package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "\"statistics\"",  // Quote table name as 'statistics' is a PostgreSQL reserved word
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exactly one Statistics row per user
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private int totalTrips = 0;

    @Column(nullable = false)
    @Builder.Default
    private double totalDistanceKm = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private int totalHazardsReported = 0;

    @Column(nullable = false)
    @Builder.Default
    private int totalVotes = 0;
}
