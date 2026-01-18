package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for device ownership information
 * Avoids circular references when serializing to JSON
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceOwnershipDTO {
    private Long id;
    private String deviceId;
    private Long userId; // User ID instead of full User object
    private String username; // Username for display
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime transferredAt;
    private String notes;
}
