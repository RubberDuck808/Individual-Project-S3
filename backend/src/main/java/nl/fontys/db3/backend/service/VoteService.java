package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.repository.VoteRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.mapper.VoteMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final HazardReportRepository hazardReportRepository;
    private final VoteMapper voteMapper;

    public VoteService(
            VoteRepository voteRepository,
            UserRepository userRepository,
            HazardReportRepository hazardReportRepository,
            VoteMapper voteMapper) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.hazardReportRepository = hazardReportRepository;
        this.voteMapper = voteMapper;
    }

    // --------------------------
    // Core voting logic
    // --------------------------
    public Vote vote(Long userId, Long hazardId, VoteType type) {

        // Prevent duplicate votes
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

    // --------------------------
    // Wrapper returning DTO
    // --------------------------
    public VoteDTO voteAsDTO(Long userId, Long hazardId, VoteType type) {
        Vote vote = vote(userId, hazardId, type);
        return voteMapper.toDTO(vote);
    }

    // --------------------------
    // Get all votes (DTO)
    // --------------------------
    public List<VoteDTO> getAllVotes() {
        return voteRepository.findAll()
                .stream()
                .map(voteMapper::toDTO)
                .toList();
    }

    // --------------------------
    // Count votes
    // --------------------------
    public long countVotes(Long hazardId, VoteType type) {
        return voteRepository.countByHazardReport_IdAndVoteType(hazardId, type);
    }
}
