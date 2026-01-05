package nl.fontys.db3.backend.service.hazard;

import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.mapper.HazardMapper;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.StatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HazardCommandService {

    private static final String HAZARD_NOT_FOUND = "Hazard not found";

    private final HazardReportRepository hazardRepo;
    private final HazardCategoryRepository categoryRepo;
    private final UserRepository userRepo;
    private final StatisticsService statisticsService;
    private final HazardWsPublisher wsPublisher;
    private final HazardMapper hazardMapper;

    public HazardCommandService(
            HazardReportRepository hazardRepo,
            HazardCategoryRepository categoryRepo,
            UserRepository userRepo,
            StatisticsService statisticsService,
            HazardWsPublisher wsPublisher,
            HazardMapper hazardMapper
    ) {
        this.hazardRepo = hazardRepo;
        this.categoryRepo = categoryRepo;
        this.userRepo = userRepo;
        this.statisticsService = statisticsService;
        this.wsPublisher = wsPublisher;
        this.hazardMapper = hazardMapper;
    }

    private void requireNonNullId(Long id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
    }

    @Transactional
    public HazardReport createHazard(HazardCreateRequestDTO dto, String creatorEmail) {
        requireNonNullId(dto.getCategoryId());

        HazardCategory category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        User user = userRepo.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        HazardReport hazard = HazardReport.builder()
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();

        HazardReport saved = hazardRepo.save(hazard);

        statisticsService.incrementHazards(user.getId());

        // Broadcast to clients
        wsPublisher.upsert(hazardMapper.toDTO(saved));

        return saved;
    }

    @Transactional
    public HazardReport verifyHazard(Long id) {
        requireNonNullId(id);

        HazardReport hazard = hazardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(HAZARD_NOT_FOUND));

        if (hazard.getStatus() == HazardStatus.RESOLVED ||
                hazard.getStatus() == HazardStatus.REJECTED) {
            throw new IllegalStateException("Cannot verify a resolved/rejected hazard.");
        }

        hazard.updateStatus(HazardStatus.VERIFIED);
        HazardReport saved = hazardRepo.save(hazard);

        wsPublisher.upsert(hazardMapper.toDTO(saved));

        return saved;
    }

    @Transactional
    public HazardReport resolveHazard(Long id) {
        requireNonNullId(id);

        HazardReport hazard = hazardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(HAZARD_NOT_FOUND));

        if (hazard.getStatus() == HazardStatus.RESOLVED) {
            throw new IllegalStateException("Hazard already resolved.");
        }

        hazard.updateStatus(HazardStatus.RESOLVED);
        HazardReport saved = hazardRepo.save(hazard);

        wsPublisher.upsert(hazardMapper.toDTO(saved));

        return saved;
    }

    @Transactional
    public HazardReport rejectHazard(Long id) {
        requireNonNullId(id);

        HazardReport hazard = hazardRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(HAZARD_NOT_FOUND));

        if (hazard.getStatus() == HazardStatus.RESOLVED) {
            throw new IllegalStateException("Resolved hazards cannot be rejected.");
        }

        hazard.updateStatus(HazardStatus.REJECTED);
        HazardReport saved = hazardRepo.save(hazard);

        wsPublisher.upsert(hazardMapper.toDTO(saved));

        return saved;
    }
}
