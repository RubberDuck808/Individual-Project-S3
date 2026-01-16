package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.CarHealthDTO;
import nl.fontys.db3.backend.dto.TelemetryEventDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryWsPublisher {

    private final SimpMessagingTemplate messaging;

    public TelemetryWsPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /**
     * Publish telemetry update event for a specific device
     * Clients can subscribe to /topic/telemetry/{deviceId} to receive updates
     */
    public void update(String deviceId, CarHealthDTO carHealth) {
        TelemetryEventDTO event = TelemetryEventDTO.update(deviceId, carHealth);
        messaging.convertAndSend("/topic/telemetry/" + deviceId, event);
        // Also broadcast to general topic for clients monitoring multiple devices
        messaging.convertAndSend("/topic/telemetry", event);
    }
}
