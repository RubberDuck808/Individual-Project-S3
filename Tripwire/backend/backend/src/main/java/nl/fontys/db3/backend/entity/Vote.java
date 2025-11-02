package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private VoteType voteType; // UPVOTE, DOWNVOTE

    @ManyToOne
    private User user;

    @ManyToOne
    @JsonIgnore
    private HazardReport hazardReport;

    private LocalDateTime createdAt;
}

