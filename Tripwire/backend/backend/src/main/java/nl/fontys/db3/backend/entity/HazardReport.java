package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class HazardReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double latitude;
    private Double longitude;

    @ManyToOne
    private HazardCategory category;

    @ManyToOne
    private User createdBy;

    @Enumerated(EnumType.STRING)
    private HazardStatus status;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "hazardReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Vote> votes = new ArrayList<>();

    // --- Lifecycle Hooks ---
    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // --- Rich model logic ---
    public LocalDateTime getLastInteractionTime() {
        return votes.isEmpty()
                ? createdAt
                : votes.stream()
                       .map(Vote::getCreatedAt)
                       .max(LocalDateTime::compareTo)
                       .orElse(createdAt);
    }

    public boolean isExpired() {
        return getLastInteractionTime()
                .plusSeconds(50)
                .isBefore(LocalDateTime.now());
    }

    public void addVote(User user, VoteType type) {
        boolean alreadyVoted = votes.stream()
                .anyMatch(v -> v.getUser().equals(user));
        if (alreadyVoted) {
            throw new IllegalStateException("User has already voted on this report.");
        }

        Vote vote = Vote.builder()
                .user(user)
                .hazardReport(this)
                .type(type)
                .createdAt(LocalDateTime.now())
                .build();

        votes.add(vote);
    }

    public long getUpvoteCount() {
        return votes.stream()
                .filter(v -> v.getType() == VoteType.UPVOTE)
                .count();
    }

    public long getDownvoteCount() {
        return votes.stream()
                .filter(v -> v.getType() == VoteType.DOWNVOTE)
                .count();
    }

    public int getScore() {
        return (int) (getUpvoteCount() - getDownvoteCount());
    }
}

