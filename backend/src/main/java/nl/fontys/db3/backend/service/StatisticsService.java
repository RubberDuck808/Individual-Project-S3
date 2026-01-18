package nl.fontys.db3.backend.service;

import lombok.extern.slf4j.Slf4j;
import nl.fontys.db3.backend.dto.UserStatsDTO;
import nl.fontys.db3.backend.entity.Statistics;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.StatisticsRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class StatisticsService {

    private static final String USER_NOT_FOUND_FOR_USER_ID = "User not found for userId=";
    private static final String STATISTICS_ROW_MISSING_FOR_USER_ID = "Statistics row missing for userId=";
    private static final String STATISTICS_ROW_MISSING_CREATING_LOG = "Statistics row missing, creating - userId: {}";

    private final StatisticsRepository statsRepo;
    private final UserRepository userRepo;
    
    public StatisticsService(
            StatisticsRepository statsRepo,
            UserRepository userRepo
    ) {
        this.statsRepo = statsRepo;
        this.userRepo = userRepo;
    }

    /**
     * Ensure a statistics row exists for a user (call at registration time).
     */
    @Transactional
    public Statistics ensureStatsRow(User user) {
        log.debug("Ensuring statistics row exists - userId: {}", user.getId());
        return statsRepo.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    log.debug("Creating new statistics row - userId: {}", user.getId());
                    return statsRepo.save(
                            Statistics.builder()
                                    .user(user)
                                    .totalTrips(0)
                                    .totalDistanceKm(0.0)
                                    .totalHazardsReported(0)
                                    .totalVotes(0)
                                    .build()
                    );
                });
    }

    /**
     * Read stats for profile summary (fast, lifetime counters).
     */
    @Transactional(readOnly = true)
    public UserStatsDTO getStatsByUsername(String username) {
        log.debug("Getting statistics by username - username: {}", username);
        if (username == null || username.isBlank()) {
            log.warn("Get statistics failed - username is required");
            throw new IllegalArgumentException("username cannot be null/blank");
        }

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Get statistics failed - user not found: username: {}", username);
                    return new IllegalArgumentException("User not found");
                });

        Statistics s = statsRepo.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    log.debug("Auto-creating statistics row - userId: {}", user.getId());
                    return createStatsRowInternal(user);
                });

        return UserStatsDTO.builder()
                .totalTrips(s.getTotalTrips())
                .totalDistanceKm(s.getTotalDistanceKm())
                .totalHazardsReported(s.getTotalHazardsReported())
                .totalVotes(s.getTotalVotes())
                .build();
    }
    
    /**
     * Internal method to create stats row without transactional annotation
     * to avoid calling transactional method via 'this'.
     */
    private Statistics createStatsRowInternal(User user) {
        return statsRepo.save(
                Statistics.builder()
                        .user(user)
                        .totalTrips(0)
                        .totalDistanceKm(0.0)
                        .totalHazardsReported(0)
                        .totalVotes(0)
                        .build()
        );
    }

    @Transactional
    public void incrementVotes(Long userId) {
        log.debug("Incrementing votes - userId: {}", userId);
        if (!statsRepo.existsByUser_Id(userId)) {
            log.debug(STATISTICS_ROW_MISSING_CREATING_LOG, userId);
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Increment votes failed - user not found: userId: {}", userId);
                        return new IllegalArgumentException(USER_NOT_FOUND_FOR_USER_ID + userId);
                    });
            createStatsRowInternal(user);
            statsRepo.flush();
        }
        int updated = statsRepo.incVotes(userId);
        if (updated == 0) {
            log.error("Increment votes failed - statistics row missing after creation: userId: {}", userId);
            throw new IllegalStateException(STATISTICS_ROW_MISSING_FOR_USER_ID + userId);
        }
        log.debug("Votes incremented successfully - userId: {}", userId);
    }

    @Transactional
    public void incrementHazards(Long userId) {
        log.debug("Incrementing hazards - userId: {}", userId);
        if (!statsRepo.existsByUser_Id(userId)) {
            log.debug(STATISTICS_ROW_MISSING_CREATING_LOG, userId);
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Increment hazards failed - user not found: userId: {}", userId);
                        return new IllegalArgumentException(USER_NOT_FOUND_FOR_USER_ID + userId);
                    });
            createStatsRowInternal(user);
            statsRepo.flush();
        }
        int updated = statsRepo.incHazards(userId);
        if (updated == 0) {
            log.error("Increment hazards failed - statistics row missing after creation: userId: {}", userId);
            throw new IllegalStateException(STATISTICS_ROW_MISSING_FOR_USER_ID + userId);
        }
        log.debug("Hazards incremented successfully - userId: {}", userId);
    }

    @Transactional
    public void incrementTripsAndDistance(Long userId, double km) {
        log.debug("Incrementing trips and distance - userId: {}, distanceKm: {}", userId, km);
        if (km < 0) {
            log.warn("Increment trips failed - negative distance: userId: {}, distanceKm: {}", userId, km);
            throw new IllegalArgumentException("distanceKm cannot be negative");
        }

        if (!statsRepo.existsByUser_Id(userId)) {
            log.debug(STATISTICS_ROW_MISSING_CREATING_LOG, userId);
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Increment trips failed - user not found: userId: {}", userId);
                        return new IllegalArgumentException(USER_NOT_FOUND_FOR_USER_ID + userId);
                    });
            createStatsRowInternal(user);
            statsRepo.flush();
        }
        int updated = statsRepo.incTripsAndAddDistance(userId, km);
        if (updated == 0) {
            log.error("Increment trips failed - statistics row missing after creation: userId: {}", userId);
            throw new IllegalStateException(STATISTICS_ROW_MISSING_FOR_USER_ID + userId);
        }
        log.debug("Trips and distance incremented successfully - userId: {}, distanceKm: {}", userId, km);
    }

}
