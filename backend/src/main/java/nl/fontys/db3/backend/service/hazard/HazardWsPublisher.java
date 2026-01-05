package nl.fontys.db3.backend.service.hazard;

import nl.fontys.db3.backend.dto.HazardEventDTO;
import nl.fontys.db3.backend.dto.HazardReportDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class HazardWsPublisher {

    private final SimpMessagingTemplate messaging;

    public HazardWsPublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void upsert(HazardReportDTO dto) {
        messaging.convertAndSend("/topic/hazards", HazardEventDTO.upsert(dto));
    }

    public void delete(Long hazardId) {
        messaging.convertAndSend("/topic/hazards", HazardEventDTO.delete(hazardId));
    }
}
