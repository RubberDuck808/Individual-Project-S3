package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.DeviceOwnershipDTO;
import nl.fontys.db3.backend.entity.DeviceOwnership;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.service.DeviceOwnershipService;
import nl.fontys.db3.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static nl.fontys.db3.backend.service.Constants.USER_NOT_FOUND;

@RestController
@RequestMapping("/api/devices")
public class DeviceOwnershipController {

    private final DeviceOwnershipService ownershipService;
    private final UserService userService;

    public DeviceOwnershipController(DeviceOwnershipService ownershipService,
                                    UserService userService) {
        this.ownershipService = ownershipService;
        this.userService = userService;
    }

    /**
     * POST /api/devices/{deviceId}/assign
     * Assign device to authenticated user
     */
    @PostMapping("/{deviceId}/assign")
    public ResponseEntity<DeviceOwnership> assignDevice(
            @PathVariable String deviceId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long userId = userService.findByUsernameOrEmail(null, userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        DeviceOwnership ownership = ownershipService.assignDeviceToUser(deviceId, userId, notes);
        return ResponseEntity.ok(ownership);
    }

    /**
     * POST /api/devices/{deviceId}/transfer
     * Transfer device to another user (requires current ownership)
     */
    @PostMapping("/{deviceId}/transfer")
    public ResponseEntity<DeviceOwnership> transferDevice(
            @PathVariable String deviceId,
            @RequestParam Long newOwnerId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long currentUserId = userService.findByUsernameOrEmail(null, userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!ownershipService.isOwner(deviceId, currentUserId)) {
            return ResponseEntity.status(403).build();
        }

        String transferNotes = notes != null ? notes : "Transferred from user " + currentUserId;
        DeviceOwnership ownership = ownershipService.assignDeviceToUser(deviceId, newOwnerId, transferNotes);
        return ResponseEntity.ok(ownership);
    }

    /**
     * DELETE /api/devices/{deviceId}/unassign
     * Unassign device from current owner
     */
    @DeleteMapping("/{deviceId}/unassign")
    public ResponseEntity<Void> unassignDevice(
            @PathVariable String deviceId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long userId = userService.findByUsernameOrEmail(null, userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        if (!ownershipService.isOwner(deviceId, userId)) {
            return ResponseEntity.status(403).build();
        }

        ownershipService.unassignDevice(deviceId, notes);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/devices/my-devices
     * Get all devices owned by authenticated user
     */
    @GetMapping("/my-devices")
    public ResponseEntity<List<DeviceOwnershipDTO>> getMyDevices(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        Long userId = userService.findByUsernameOrEmail(null, userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        List<DeviceOwnership> devices = ownershipService.getDevicesByUser(userId);
        
        List<DeviceOwnershipDTO> deviceDTOs = devices.stream()
                .map(ownership -> DeviceOwnershipDTO.builder()
                        .id(ownership.getId())
                        .deviceId(ownership.getDeviceId())
                        .userId(ownership.getUser() != null ? ownership.getUser().getId() : null)
                        .username(ownership.getUser() != null ? ownership.getUser().getUsername() : null)
                        .active(ownership.isActive())
                        .createdAt(ownership.getCreatedAt())
                        .transferredAt(ownership.getTransferredAt())
                        .notes(ownership.getNotes())
                        .build())
                        .toList();
        
        return ResponseEntity.ok(deviceDTOs);
    }

    /**
     * GET /api/devices/{deviceId}/ownership
     * Get ownership history for a device
     */
    @GetMapping("/{deviceId}/ownership")
    public ResponseEntity<List<DeviceOwnership>> getOwnershipHistory(@PathVariable String deviceId) {
        List<DeviceOwnership> history = ownershipService.getOwnershipHistory(deviceId);
        return ResponseEntity.ok(history);
    }
}
