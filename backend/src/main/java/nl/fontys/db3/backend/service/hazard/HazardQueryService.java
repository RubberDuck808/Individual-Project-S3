package nl.fontys.db3.backend.service.hazard;

import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HazardQueryService {

    private final HazardReportRepository hazardRepo;

    @Value("${MAPBOX_TOKEN}")
    private String mapboxToken;

    public HazardQueryService(HazardReportRepository hazardRepo) {
        this.hazardRepo = hazardRepo;
    }

    public List<HazardReport> getOpenHazards() {
        return hazardRepo.findByStatus(HazardStatus.OPEN).stream()
                .filter(h -> !h.isExpired())
                .toList();
    }

    public List<HazardReport> getActiveHazards() {
        return hazardRepo.findByStatusIn(List.of(
                HazardStatus.OPEN,
                HazardStatus.VERIFIED
        )).stream()
                .filter(h -> !h.isExpired())
                .toList();
    }

    public HazardReport getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Hazard ID cannot be null");
        }

        return hazardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hazard not found"));
    }

    public List<HazardReport> getHazardsByUsername(String username) {
    if (username == null || username.isBlank()) {
        throw new IllegalArgumentException("Username cannot be null/blank");
    }

    return hazardRepo.findByCreatedByUsernameOrderByIdDesc(username);
}

public List<HazardReport> getActiveHazardsByUsername(String username) {
    if (username == null || username.isBlank()) {
        throw new IllegalArgumentException("Username cannot be null/blank");
    }

    return hazardRepo.findByCreatedByUsernameOrderByIdDesc(username).stream()
            .filter(h -> !h.isExpired())
            .toList();
}


}
