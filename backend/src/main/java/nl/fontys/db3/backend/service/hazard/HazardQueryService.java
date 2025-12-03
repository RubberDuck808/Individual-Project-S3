package nl.fontys.db3.backend.service.hazard;

import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HazardQueryService {

    private final HazardReportRepository hazardRepo;

    public HazardQueryService(HazardReportRepository hazardRepo) {
        this.hazardRepo = hazardRepo;
    }

    /** OPEN hazards only (your current behavior) */
    public List<HazardReport> getOpenHazards() {
        return hazardRepo.findByStatus(HazardStatus.OPEN).stream()
                .filter(h -> !h.isExpired())
                .toList();
    }

    /** ACTIVE hazards = OPEN + VERIFIED */
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

}
