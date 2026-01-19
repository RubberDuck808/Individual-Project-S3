package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
    name = "background",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "image_path")
    }
)
@Getter
@Setter
@SuperBuilder
public class Background extends BaseImageAsset {
    // All fields inherited from BaseImageAsset:
    // - id, name, imagePath, active
}
