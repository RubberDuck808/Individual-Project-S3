package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.dto.VoteRequestDTO;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    /** GET all votes */
    @GetMapping
    public ResponseEntity<List<VoteDTO>> getAllVotes() {
        List<VoteDTO> votes = voteService.getAllVotes();
        return ResponseEntity.ok(votes);
    }

    /** POST submit a vote */
    @PostMapping
    public ResponseEntity<VoteDTO> voteJson(@RequestBody VoteRequestDTO dto) {
        try {
            VoteType type = VoteType.valueOf(dto.getVoteType().toUpperCase());
            VoteDTO savedVote = voteService.voteAsDTO(dto.getUserId(), dto.getHazardId(), type);
            return ResponseEntity.ok(savedVote);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid vote type: " + dto.getVoteType(), e);
        }
    }

    /** GET vote counts for a hazard (JSON response) */
    @GetMapping("/{hazardId}/count")
    public ResponseEntity<Map<String, Long>> getVotes(@PathVariable Long hazardId) {
        long upvotes = voteService.countVotes(hazardId, VoteType.UPVOTE);
        long downvotes = voteService.countVotes(hazardId, VoteType.DOWNVOTE);

        Map<String, Long> counts = new HashMap<>();
        counts.put("upvotes", upvotes);
        counts.put("downvotes", downvotes);

        return ResponseEntity.ok(counts);
    }
}
