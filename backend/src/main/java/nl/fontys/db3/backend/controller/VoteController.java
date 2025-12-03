package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.dto.VoteRequestDTO;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.service.VoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    // Get ALL votes
    @GetMapping
    public ResponseEntity<?> getAllVotes() {
        // voteService.getAllVotes() already returns List<VoteDTO>
        return ResponseEntity.ok(voteService.getAllVotes());
    }

    // Submit vote as JSON body
    @PostMapping
    public ResponseEntity<?> voteJson(@RequestBody VoteRequestDTO dto) {
        try {
            VoteType type = VoteType.valueOf(dto.getVoteType().toUpperCase());
            VoteDTO savedVote = voteService.voteAsDTO(dto.getUserId(), dto.getHazardId(), type);
            return ResponseEntity.ok(savedVote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get vote counts (text response)
    @GetMapping("/{hazardId}/count")
    public ResponseEntity<?> getVotes(@PathVariable Long hazardId) {
        long upvotes = voteService.countVotes(hazardId, VoteType.UPVOTE);
        long downvotes = voteService.countVotes(hazardId, VoteType.DOWNVOTE);

        return ResponseEntity.ok(
            String.format("Upvotes: %d, Downvotes: %d", upvotes, downvotes)
        );
    }
}
