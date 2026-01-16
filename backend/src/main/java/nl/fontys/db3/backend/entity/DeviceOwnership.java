package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Tracks device ownership over time
 * Allows device ownership transfer without losing telemetry history
 * Supports historical tracking of who owned which device when
 */
@Entity
@Table(name = "device_ownership", indexes = {
    @Index(name = "idx_device_ownership_device", columnList = "device_id"),
    @Index(name = "idx_device_ownership_user", columnList = "user_id"),
    @Index(name = "idx_device_ownership_active", columnList = "active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOwnership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId; // Reference to device (not FK to allow flexibility)

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Current owner (null if unassigned)

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true; // Only one active ownership per device

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "transferred_at")
    private LocalDateTime transferredAt; // When ownership was transferred (null if still active)

    @Column(length = 500)
    private String notes; // Optional notes about ownership transfer
}
