package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Base class for image assets (Avatar, Background)
 * Used to reduce code duplication
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseImageAsset {

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
