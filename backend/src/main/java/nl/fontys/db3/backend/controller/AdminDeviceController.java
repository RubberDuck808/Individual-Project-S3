package nl.fontys.db3.backend.controller;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.AdminDeviceDTO;
import nl.fontys.db3.backend.service.AdminDeviceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/devices")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDeviceController {

    private final AdminDeviceService deviceService;

    /**
     * GET /api/admin/devices
     * Get all devices (paginated)
     */
    @GetMapping
    public ResponseEntity<Page<AdminDeviceDTO>> getAllDevices(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AdminDeviceDTO> devices = deviceService.getAllDevices(pageable);
        return ResponseEntity.ok(devices);
    }

    /**
     * GET /api/admin/devices/{id}
     * Get device by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminDeviceDTO> getDeviceById(@PathVariable Long id) {
        AdminDeviceDTO device = deviceService.getDeviceById(id);
        return ResponseEntity.ok(device);
    }

    /**
     * GET /api/admin/devices/device-id/{deviceId}
     * Get device by deviceId
     */
    @GetMapping("/device-id/{deviceId}")
    public ResponseEntity<AdminDeviceDTO> getDeviceByDeviceId(@PathVariable String deviceId) {
        AdminDeviceDTO device = deviceService.getDeviceByDeviceId(deviceId);
        return ResponseEntity.ok(device);
    }

    /**
     * PUT /api/admin/devices/{id}/activate
     * Activate device
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateDevice(@PathVariable Long id) {
        deviceService.activateDevice(id);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/admin/devices/{id}/deactivate
     * Deactivate device
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateDevice(@PathVariable Long id) {
        deviceService.deactivateDevice(id);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/admin/devices/{id}/description
     * Update device description
     */
    @PutMapping("/{id}/description")
    public ResponseEntity<Void> updateDescription(
            @PathVariable Long id,
            @RequestParam String description) {
        deviceService.updateDeviceDescription(id, description);
        return ResponseEntity.ok().build();
    }
}
