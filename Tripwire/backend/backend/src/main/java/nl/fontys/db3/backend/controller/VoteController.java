package nl.fontys.db3.backend.controller;

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

    @GetMapping
    public ResponseEntity<?> getAllVotes() {
        return ResponseEntity.ok(voteService.getAllVotes());
    }


    @PostMapping("/{hazardId}/user/{userId}")
    public ResponseEntity<?> vote(@PathVariable Long hazardId,
                                  @PathVariable Long userId,
                                  @RequestParam VoteType type) {
        try {
            Vote vote = voteService.vote(userId, hazardId, type);
            return ResponseEntity.ok(vote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{hazardId}/count")
    public ResponseEntity<?> getVotes(@PathVariable Long hazardId) {
        long upvotes = voteService.countVotes(hazardId, VoteType.UPVOTE);
        long downvotes = voteService.countVotes(hazardId, VoteType.DOWNVOTE);
        return ResponseEntity.ok(
                String.format("Upvotes: %d, Downvotes: %d", upvotes, downvotes)
        );
    }
}
