package nl.fontys.db3.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationResponseDTO {
    private Long deviceId;
    private String deviceIdentifier;
    private String apiKey; // Show only once! Store securely on ESP32
    private String message;
}
