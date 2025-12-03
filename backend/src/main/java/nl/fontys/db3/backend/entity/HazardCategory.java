package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class HazardCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // For UI icons on MapBox (optional but very useful)
    private String icon;

    // If you want to disable categories instead of deleting them
    @Builder.Default
    private boolean active = true;
}

