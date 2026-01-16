package nl.fontys.db3.backend.controller;

import lombok.extern.slf4j.Slf4j;
import nl.fontys.db3.backend.dto.DeviceRegistrationRequestDTO;
import nl.fontys.db3.backend.dto.DeviceRegistrationResponseDTO;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.service.DeviceService;
import nl.fontys.db3.backend.service.DeviceOwnershipService;
import nl.fontys.db3.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceOwnershipService ownershipService;
    private final UserService userService;

    public DeviceController(DeviceService deviceService,
                           DeviceOwnershipService ownershipService,
                           UserService userService) {
        this.deviceService = deviceService;
        this.ownershipService = ownershipService;
        this.userService = userService;
    }

    /**
     * POST /api/devices/register
     * Register a new device and get API key
     * Optionally assigns device to authenticated user
     * Requires user authentication
     */
    @PostMapping("/register")
    public ResponseEntity<DeviceRegistrationResponseDTO> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails != null ? userDetails.getUsername() : "anonymous";
        log.info("Device registration request - deviceId: {}, username: {}", dto.getDeviceId(), username);
        
        try {
            DeviceService.DeviceRegistrationResult result = deviceService.registerDevice(
                dto.getDeviceId(),
                dto.getDescription()
            );
            
            if (userDetails != null) {
                Long userId = userService.findByUsernameOrEmail(null, userDetails.getUsername())
                        .map(User::getId)
                        .orElse(null);
                
                if (userId != null) {
                    List<nl.fontys.db3.backend.entity.DeviceOwnership> userDevices = 
                        ownershipService.getDevicesByUser(userId);
                    if (!userDevices.isEmpty()) {
                        log.warn("Device registration failed - user already has device: userId: {}, deviceId: {}", 
                                userId, dto.getDeviceId());
                        throw new IllegalArgumentException("User already has a device. Maximum 1 device per user. Please remove your current device first.");
                    }
                    
                    ownershipService.assignDeviceToUser(
                        dto.getDeviceId(),
                        userId,
                        "Initial device registration"
                    );
                    log.info("Device assigned to user - deviceId: {}, userId: {}", dto.getDeviceId(), userId);
                }
            }

            DeviceRegistrationResponseDTO response = DeviceRegistrationResponseDTO.builder()
                    .deviceId(result.getDevice().getId())
                    .deviceIdentifier(result.getDevice().getDeviceId())
                    .apiKey(result.getApiKey())
                    .message("Device registered. Store this API key securely on your ESP32. It will not be shown again.")
                    .build();

            log.info("Device registered successfully - deviceId: {}, deviceDbId: {}", 
                    dto.getDeviceId(), result.getDevice().getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Device registration failed - deviceId: {}, reason: {}", dto.getDeviceId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error registering device - deviceId: {}", dto.getDeviceId(), e);
            throw e;
        }
    }
}
