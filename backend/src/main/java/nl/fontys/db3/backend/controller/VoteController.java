package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.dto.VoteRequestDTO;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    /** All votes */

    @GetMapping
    public ResponseEntity<List<VoteDTO>> getAllVotes() {
        List<VoteDTO> votes = voteService.getAllVotes();
        return ResponseEntity.ok(votes);
    }

    /** Submit a vote */
    @PostMapping
    public ResponseEntity<VoteDTO> voteJson(@RequestBody VoteRequestDTO dto, Authentication auth) {
        VoteType type;
        try {
            type = VoteType.valueOf(dto.getVoteType().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid vote type: " + dto.getVoteType(), e);
        }

        String email = auth.getName();
        VoteDTO savedVote = voteService.voteAsDTO(email, dto.getHazardId(), type);
        return ResponseEntity.ok(savedVote);
    }


    /** Vote counts for a hazard */
    @GetMapping("/{hazardId}/count")
    public ResponseEntity<Map<String, Long>> getVotes(@PathVariable Long hazardId) {
        long upvotes = voteService.countVotes(hazardId, VoteType.UPVOTE);
        long downvotes = voteService.countVotes(hazardId, VoteType.DOWNVOTE);

        Map<String, Long> counts = new HashMap<>();
        counts.put("upvotes", upvotes);
        counts.put("downvotes", downvotes);

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/{hazardId}/mine")
    public ResponseEntity<Map<String, String>> myVote(@PathVariable Long hazardId, Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(voteService.getMyVote(email, hazardId));
    }
}
