package nl.fontys.db3.backend.service.hazard;

import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class HazardCommandService {

    private static final String HAZARD_NOT_FOUND = "Hazard not found";

    private final HazardReportRepository hazardRepo;
    private final HazardCategoryRepository categoryRepo;
    private final UserRepository userRepo;

    public HazardCommandService(
            HazardReportRepository hazardRepo,
            HazardCategoryRepository categoryRepo,
            UserRepository userRepo
    ) {
        this.hazardRepo = hazardRepo;
        this.categoryRepo = categoryRepo;
        this.userRepo = userRepo;
    }

    private void requireNonNullId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }

    public HazardReport createHazard(HazardCreateRequestDTO dto) {

        requireNonNullId(dto.getCategoryId());
        requireNonNullId(dto.getCreatedBy());

        HazardCategory category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        User user = userRepo.findById(dto.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        HazardReport hazard = HazardReport.builder()
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();

        return hazardRepo.save(hazard);
    }


    /** Verify hazard */
    public HazardReport verifyHazard(Long id) {
        requireNonNullId(id);

        HazardReport hazard = hazardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(HAZARD_NOT_FOUND));

        if (hazard.getStatus() == HazardStatus.RESOLVED ||
            hazard.getStatus() == HazardStatus.REJECTED) {
            throw new IllegalStateException("Cannot verify a resolved/rejected hazard.");
        }

        hazard.updateStatus(HazardStatus.VERIFIED);
        return hazardRepo.save(hazard);
    }

    /** Resolve hazard */
    public HazardReport resolveHazard(Long id) {
        requireNonNullId(id);

        HazardReport hazard = hazardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(HAZARD_NOT_FOUND));

        if (hazard.getStatus() == HazardStatus.RESOLVED) {
            throw new IllegalStateException("Hazard already resolved.");
        }

        hazard.updateStatus(HazardStatus.RESOLVED);
        return hazardRepo.save(hazard);
    }

    /** Reject hazard */
    public HazardReport rejectHazard(Long id) {
        requireNonNullId(id);

        HazardReport hazard = hazardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(HAZARD_NOT_FOUND));

        if (hazard.getStatus() == HazardStatus.RESOLVED) {
            throw new IllegalStateException("Resolved hazards cannot be rejected.");
        }

        hazard.updateStatus(HazardStatus.REJECTED);
        return hazardRepo.save(hazard);
    }
}
