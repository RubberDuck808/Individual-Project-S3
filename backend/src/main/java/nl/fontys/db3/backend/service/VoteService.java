package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.entity.Vote;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class VoteService {

    private final VoteRepository voteRepo;
    private final UserRepository userRepo;
    private final HazardReportRepository hazardRepo;
    private final StatisticsService statisticsService;

    public VoteService(
            VoteRepository voteRepo,
            UserRepository userRepo,
            HazardReportRepository hazardRepo,
            StatisticsService statisticsService
    ) {
        this.voteRepo = voteRepo;
        this.userRepo = userRepo;
        this.hazardRepo = hazardRepo;
        this.statisticsService = statisticsService;
    }

    public List<VoteDTO> getAllVotes() {
        return voteRepo.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional
    public VoteDTO voteAsDTO(String userEmail, Long hazardId, VoteType type) {
        if (hazardId == null) throw new IllegalArgumentException("hazardId cannot be null");

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        HazardReport hazard = hazardRepo.findById(hazardId)
                .orElseThrow(() -> new IllegalArgumentException("Hazard not found"));

        // 1) Creator cannot vote on own report
        if (hazard.getCreatedBy() != null &&
                hazard.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot vote on your own report");
        }

        // 2) User can only vote once (no switching)
        if (voteRepo.existsByHazardReport_IdAndUser_Id(hazardId, user.getId())) {
            throw new IllegalArgumentException("You already voted on this report");
        }

        Vote vote = Vote.builder()
                .hazardReport(hazard)
                .user(user)
                .voteType(type)
                .build();

        Vote saved = voteRepo.save(vote);

        statisticsService.incrementVotes(user.getId());

        return toDTO(saved);
    }

    public long countVotes(Long hazardId, VoteType type) {
        return voteRepo.countByHazardReport_IdAndVoteType(hazardId, type);
    }

    private VoteDTO toDTO(Vote vote) {
        return VoteDTO.builder()
                .id(vote.getId())
                .voteType(vote.getVoteType().name())
                .userId(vote.getUser().getId())
                .hazardId(vote.getHazardReport().getId())
                .build();
    }

    @Transactional(readOnly = true)
    public long getLifetimeVotesCastByUser(String username) {
        return statisticsService.getStatsByUsername(username).getTotalVotes();
    }

    public Map<String, String> getMyVote(String userEmail, Long hazardId) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return voteRepo.findByHazardReport_IdAndUser_Id(hazardId, user.getId())
                .map(v -> Map.of("voteType", v.getVoteType().name()))
                .orElseGet(Map::of);
    }
}
