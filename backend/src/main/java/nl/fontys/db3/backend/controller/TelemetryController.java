package nl.fontys.db3.backend.controller;

import lombok.extern.slf4j.Slf4j;
import nl.fontys.db3.backend.dto.CarHealthDTO;
import nl.fontys.db3.backend.dto.LiveTelemetryDTO;
import nl.fontys.db3.backend.dto.LiveTelemetryRequestDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryRequestDTO;
import nl.fontys.db3.backend.service.LiveTelemetryService;
import nl.fontys.db3.backend.service.TelemetryHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final LiveTelemetryService liveTelemetryService;
    private final TelemetryHistoryService historyService;

    public TelemetryController(LiveTelemetryService liveTelemetryService,
                               TelemetryHistoryService historyService) {
        this.liveTelemetryService = liveTelemetryService;
        this.historyService = historyService;
    }

    @PutMapping("/live")
    public ResponseEntity<LiveTelemetryDTO> upsertLive(@Valid @RequestBody LiveTelemetryRequestDTO dto) {
        log.debug("Upserting live telemetry - deviceId: {}", dto.getDeviceId());
        try {
            LiveTelemetryDTO saved = liveTelemetryService.upsert(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error upserting live telemetry - deviceId: {}", dto.getDeviceId(), e);
            throw e;
        }
    }

    @GetMapping("/live/{deviceId}")
    public ResponseEntity<LiveTelemetryDTO> getLive(@PathVariable String deviceId) {
        Optional<LiveTelemetryDTO> live = liveTelemetryService.getByDeviceId(deviceId);
        
        if (live.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(live.get());
    }

    @PostMapping("/history")
    public ResponseEntity<TelemetryHistoryDTO> storeHistory(@Valid @RequestBody TelemetryHistoryRequestDTO dto) {
        log.debug("Storing telemetry history - deviceId: {}", dto.getDeviceId());
        try {
            TelemetryHistoryDTO saved = historyService.store(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error storing telemetry history - deviceId: {}", dto.getDeviceId(), e);
            throw e;
        }
    }

    @GetMapping("/history/{deviceId}")
    public ResponseEntity<List<TelemetryHistoryDTO>> getHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "50") int limit) {
        List<TelemetryHistoryDTO> history = historyService.getLatest(deviceId, limit);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{deviceId}/range")
    public ResponseEntity<List<TelemetryHistoryDTO>> getHistoryRange(
            @PathVariable String deviceId,
            @RequestParam Instant start,
            @RequestParam Instant end,
            @RequestParam(defaultValue = "100") int limit) {
        List<TelemetryHistoryDTO> history = historyService.getHistory(deviceId, start, end, limit);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/device/{deviceId}/health")
    public ResponseEntity<CarHealthDTO> getCarHealth(@PathVariable String deviceId) {
        Optional<CarHealthDTO> health = historyService.getLatestCarHealth(deviceId);
        
        if (health.isEmpty()) {
            CarHealthDTO disconnected = CarHealthDTO.builder()
                    .connected(false)
                    .build();
            return ResponseEntity.ok(disconnected);
        }
        
        return ResponseEntity.ok(health.get());
    }

    @GetMapping("/device/{deviceId}/health/history")
    public ResponseEntity<List<CarHealthDTO>> getCarHealthHistory(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "50") int limit) {
        List<CarHealthDTO> history = historyService.getCarHealthHistory(deviceId, limit);
        return ResponseEntity.ok(history);
    }
}
