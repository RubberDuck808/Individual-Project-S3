package nl.fontys.db3.backend.service;

import lombok.extern.slf4j.Slf4j;
import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.mapper.HazardMapper;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.hazard.HazardWsPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class HazardService {

    private static final String HAZARD_NOT_FOUND = "Hazard not found";

    private final HazardReportRepository hazardRepository;
    private final UserRepository userRepository;
    private final HazardCategoryService categoryService;
    private final StatisticsService statisticsService;
    private final HazardWsPublisher wsPublisher;
    private final HazardMapper hazardMapper;

    public HazardService(
            HazardReportRepository hazardRepository,
            UserRepository userRepository,
            HazardCategoryService categoryService,
            StatisticsService statisticsService,
            HazardWsPublisher wsPublisher,
            HazardMapper hazardMapper
    ) {
        this.hazardRepository = hazardRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.statisticsService = statisticsService;
        this.wsPublisher = wsPublisher;
        this.hazardMapper = hazardMapper;
    }

    @Transactional
    public HazardReport createHazard(HazardCreateRequestDTO dto, String creatorEmail) {
        log.info("Creating hazard - creatorEmail: {}, categoryId: {}, lat: {}, lng: {}", 
                creatorEmail, dto.getCategoryId(), dto.getLatitude(), dto.getLongitude());
        
        if (dto.getCategoryId() == null) {
            log.warn("Hazard creation failed - category ID is null");
            throw new IllegalArgumentException("Category ID cannot be null");
        }

        HazardCategory category = categoryService.getCategoryById(dto.getCategoryId());
        User user = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> {
                    log.warn("Hazard creation failed - user not found: email: {}", creatorEmail);
                    return new IllegalArgumentException("User not found");
                });

        HazardReport hazard = HazardReport.builder()
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .category(category)
                .createdBy(user)
                .status(HazardStatus.OPEN)
                .build();

        HazardReport saved = hazardRepository.save(hazard);
        statisticsService.incrementHazards(user.getId());
        wsPublisher.upsert(hazardMapper.toDTO(saved));

        log.info("Hazard created successfully - hazardId: {}, userId: {}, category: {}", 
                saved.getId(), user.getId(), category.getName());
        return saved;
    }

    @Transactional
    public HazardReport verifyHazard(Long id) {
        log.info("Verifying hazard - hazardId: {}", id);
        HazardReport hazard = findHazardById(id, "verification");
        
        if (hazard.getStatus() == HazardStatus.RESOLVED ||
                hazard.getStatus() == HazardStatus.REJECTED) {
            log.warn("Hazard verification failed - cannot verify resolved/rejected hazard: hazardId: {}, status: {}", 
                    id, hazard.getStatus());
            throw new IllegalStateException("Cannot verify a resolved/rejected hazard.");
        }

        return updateHazardStatus(hazard, HazardStatus.VERIFIED, id, "verified");
    }

    @Transactional
    public HazardReport resolveHazard(Long id) {
        log.info("Resolving hazard - hazardId: {}", id);
        HazardReport hazard = findHazardById(id, "resolution");
        
        if (hazard.getStatus() == HazardStatus.RESOLVED) {
            log.warn("Hazard resolution failed - already resolved: hazardId: {}", id);
            throw new IllegalStateException("Hazard already resolved.");
        }

        return updateHazardStatus(hazard, HazardStatus.RESOLVED, id, "resolved");
    }

    @Transactional
    public HazardReport rejectHazard(Long id) {
        log.info("Rejecting hazard - hazardId: {}", id);
        HazardReport hazard = findHazardById(id, "rejection");
        
        if (hazard.getStatus() == HazardStatus.RESOLVED) {
            log.warn("Hazard rejection failed - resolved hazards cannot be rejected: hazardId: {}", id);
            throw new IllegalStateException("Resolved hazards cannot be rejected.");
        }

        return updateHazardStatus(hazard, HazardStatus.REJECTED, id, "rejected");
    }

    /**
     * Helper method to find hazard by ID with validation
     */
    private HazardReport findHazardById(Long id, String operation) {
        if (id == null) {
            log.warn("Hazard {} failed - hazard ID is null", operation);
            throw new IllegalArgumentException("Hazard ID cannot be null");
        }

        return hazardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Hazard {} failed - hazard not found: hazardId: {}", operation, id);
                    return new IllegalArgumentException(HAZARD_NOT_FOUND);
                });
    }

    /**
     * Helper method to update hazard status and publish WebSocket event
     */
    private HazardReport updateHazardStatus(HazardReport hazard, HazardStatus newStatus, Long id, String action) {
        hazard.updateStatus(newStatus);
        HazardReport saved = hazardRepository.save(hazard);
        wsPublisher.upsert(hazardMapper.toDTO(saved));
        
        log.info("Hazard {} successfully - hazardId: {}", action, id);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<HazardReport> getOpenHazards() {
        return hazardRepository.findByStatus(HazardStatus.OPEN).stream()
                .filter(h -> !h.isExpired())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HazardReport> getActiveHazards() {
        return hazardRepository.findByStatusIn(List.of(
                HazardStatus.OPEN,
                HazardStatus.VERIFIED
        )).stream()
                .filter(h -> !h.isExpired())
                .toList();
    }

    @Transactional(readOnly = true)
    public HazardReport getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Hazard ID cannot be null");
        }
        return hazardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(HAZARD_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<HazardReport> getHazardsByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null/blank");
        }
        // Check if user exists first
        if (!userRepository.existsByUsername(username.trim().toLowerCase())) {
            throw new IllegalArgumentException("User not found");
        }
        return hazardRepository.findByCreatedByUsernameOrderByIdDesc(username);
    }

    @Transactional(readOnly = true)
    public List<HazardReport> getActiveHazardsByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null/blank");
        }
        return hazardRepository.findByCreatedByUsernameOrderByIdDesc(username).stream()
                .filter(h -> !h.isExpired())
                .toList();
    }
}
