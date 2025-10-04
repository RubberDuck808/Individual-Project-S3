package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private VoteType type; // UPVOTE, DOWNVOTE

    @ManyToOne
    private User user;

    @ManyToOne
    private HazardReport hazardReport;

    private LocalDateTime createdAt;
}

