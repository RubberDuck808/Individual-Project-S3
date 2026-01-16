package nl.fontys.db3.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationRequestDTO {
    @jakarta.validation.constraints.NotBlank
    private String deviceId;
    
    private Long userId; // Optional: link to user
    
    private String description; // Optional: user-friendly description
    private String deviceType; // Optional: e.g., "ESP32"
    private String firmwareVersion; // Optional
}
