package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.VoteDTO;
import nl.fontys.db3.backend.dto.VoteRequestDTO;
import nl.fontys.db3.backend.entity.Vote;
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

    // Get all votes
    @GetMapping
    public ResponseEntity<?> getAllVotes() {
        return ResponseEntity.ok(voteService.getAllVotes());
    }

    
    @PostMapping
    public ResponseEntity<?> voteJson(@RequestBody VoteRequestDTO dto) {
        try {
            VoteType type = VoteType.valueOf(dto.getVoteType().toUpperCase());
            Vote vote = voteService.vote(dto.getUserId(), dto.getHazardId(), type);
            VoteDTO response = voteService.toDTO(vote);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Get vote counts
    @GetMapping("/{hazardId}/count")
    public ResponseEntity<?> getVotes(@PathVariable Long hazardId) {
        long upvotes = voteService.countVotes(hazardId, VoteType.UPVOTE);
        long downvotes = voteService.countVotes(hazardId, VoteType.DOWNVOTE);
        return ResponseEntity.ok(
                String.format("Upvotes: %d, Downvotes: %d", upvotes, downvotes)
        );
    }
}
