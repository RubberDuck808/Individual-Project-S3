package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private Double distanceKm;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @ManyToOne
    private FavouriteLocation startLocation;

    @ManyToOne
    private FavouriteLocation endLocation;
}
