package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Registered device entity
 * Links ESP32 devices to users and stores API keys
 */
@Entity
@Table(name = "device", uniqueConstraints = {
    @UniqueConstraint(columnNames = "device_id"),
    @UniqueConstraint(columnNames = "api_key_hash")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId;

    @Column(name = "api_key_hash", nullable = false, unique = true, length = 256)
    private String apiKeyHash;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(length = 500)
    private String description;

    @Column(name = "device_type")
    private String deviceType;
    
    @Column(name = "firmware_version")
    private String firmwareVersion;
}
