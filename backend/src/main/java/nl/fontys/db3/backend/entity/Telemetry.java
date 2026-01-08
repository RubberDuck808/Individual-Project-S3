package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "telemetry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Telemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Instant timestamp;

    private Double speedKph;
    private Double rpm;
    private Double throttlePct;
}
