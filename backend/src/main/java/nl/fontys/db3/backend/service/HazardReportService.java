package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.VoteRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HazardReportService {
    private final HazardReportRepository hazardRepo;
    private final VoteRepository voteRepo;

    public HazardReportService(HazardReportRepository hazardRepo, VoteRepository voteRepo) {
        this.hazardRepo = hazardRepo;
        this.voteRepo = voteRepo;
    }

    public List<HazardReport> getAllOpenHazards() {
        return hazardRepo.findByStatus(HazardStatus.OPEN).stream()
                .filter(h -> !h.isExpired()) // rich model rule
                .toList();
    }

    public HazardReport createHazard(HazardReport hazard) {
        hazard.setStatus(HazardStatus.OPEN);
        return hazardRepo.save(hazard);
    }

    public HazardReport addVote(Long hazardId, User user, VoteType type) {
        HazardReport report = hazardRepo.findById(hazardId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.addVote(user, type);   // uses your rich model rule
        return hazardRepo.save(report);
    }
}

