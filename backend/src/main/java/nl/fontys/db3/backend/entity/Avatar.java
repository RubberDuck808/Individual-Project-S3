package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "avatar",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "image_path")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Avatar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Display name for admin / UI, e.g. "Robot Blue"
    @Column(nullable = false)
    private String name;

    // Path in Google Bucket, e.g. "avatars/robot-blue.png"
    @Column(name = "image_path", nullable = false)
    private String imagePath;

    // If false, users can no longer select it
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
