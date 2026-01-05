package nl.fontys.db3.backend.service;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.UserStatsDTO;
import nl.fontys.db3.backend.entity.Statistics;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.StatisticsRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statsRepo;
    private final UserRepository userRepo;

    /**
     * Ensure a statistics row exists for a user (call at registration time).
     */
    @Transactional
    public Statistics ensureStatsRow(User user) {
        return statsRepo.findByUser_Id(user.getId())
                .orElseGet(() -> statsRepo.save(
                        Statistics.builder()
                                .user(user)
                                .totalTrips(0)
                                .totalDistanceKm(0.0)
                                .totalHazardsReported(0)
                                .totalVotes(0)
                                .build()
                ));
    }

    /**
     * Read stats for profile summary (fast, lifetime counters).
     */
    @Transactional(readOnly = true)
    public UserStatsDTO getStatsByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be null/blank");
        }

        // If you prefer: statsRepo.findByUser_Username(username) directly
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Statistics s = statsRepo.findByUser_Id(user.getId())
                .orElseGet(() -> ensureStatsRow(user)); // auto-heal if missing

        return UserStatsDTO.builder()
                .totalTrips(s.getTotalTrips())
                .totalDistanceKm(s.getTotalDistanceKm())
                .totalHazardsReported(s.getTotalHazardsReported())
                .totalVotes(s.getTotalVotes())
                .build();
    }

    // --- Lifetime increments (call these when events are created successfully) ---

    @Transactional
    public void incrementVotes(Long userId) {
        int updated = statsRepo.incVotes(userId);
        if (updated == 0) throw new IllegalStateException("Statistics row missing for userId=" + userId);
    }

    @Transactional
    public void incrementHazards(Long userId) {
        int updated = statsRepo.incHazards(userId);
        if (updated == 0) throw new IllegalStateException("Statistics row missing for userId=" + userId);
    }

    @Transactional
    public void incrementTripsAndDistance(Long userId, double km) {
        if (km < 0) throw new IllegalArgumentException("distanceKm cannot be negative");

        int updated = statsRepo.incTripsAndAddDistance(userId, km);
        if (updated == 0) throw new IllegalStateException("Statistics row missing for userId=" + userId);
    }

}
