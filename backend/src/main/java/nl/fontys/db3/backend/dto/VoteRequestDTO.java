package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequestDTO {
    private Long userId;
    private Long hazardId;
    private String voteType; // "UPVOTE" or "DOWNVOTE"
}
