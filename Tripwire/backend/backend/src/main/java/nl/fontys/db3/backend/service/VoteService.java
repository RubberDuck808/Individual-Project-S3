package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.repository.VoteRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final HazardReportRepository hazardReportRepository;

    public VoteService(VoteRepository voteRepository,
                       UserRepository userRepository,
                       HazardReportRepository hazardReportRepository) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.hazardReportRepository = hazardReportRepository;
    }

    public VoteDTO toDTO(Vote vote) {
        return VoteDTO.builder()
                .id(vote.getId())
                .voteType(vote.getVoteType().name())
                .userId(vote.getUser().getId())
                .hazardId(vote.getHazardReport().getId())
                .build();
    }

    public List<Vote> getAllVotes() {
        return voteRepository.findAll();
    }

    public Vote vote(Long userId, Long hazardId, VoteType type) {
        // Check if user already voted
        if (voteRepository.existsByHazardReport_IdAndUser_Id(hazardId, userId)) {
            throw new IllegalArgumentException("User already voted on this hazard");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        HazardReport hazard = hazardReportRepository.findById(hazardId)
                .orElseThrow(() -> new IllegalArgumentException("Hazard not found"));

        Vote vote = Vote.builder()
                .voteType(type)
                .user(user)
                .hazardReport(hazard)
                .createdAt(LocalDateTime.now())
                .build();

        return voteRepository.save(vote);
    }

    public long countVotes(Long hazardId, VoteType type) {
        return voteRepository.countByHazardReport_IdAndVoteType(hazardId, type);
    }
}
