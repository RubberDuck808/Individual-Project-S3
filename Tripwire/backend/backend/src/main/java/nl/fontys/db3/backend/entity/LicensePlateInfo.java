package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LicensePlateInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licensePlate;
    private String brand;
    private String model;
    private Integer yearOfManufacture;
    private String fuelType;

    private LocalDateTime fetchedAt;

    @ManyToOne
    private User user; // optional: if license plates belong to users
}
