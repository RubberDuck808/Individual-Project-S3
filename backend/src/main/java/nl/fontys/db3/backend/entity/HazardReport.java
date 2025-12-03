package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class HazardReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;

    @ManyToOne
    private HazardCategory category;

    @ManyToOne
    @JsonIgnore
    private User createdBy;

    @Enumerated(EnumType.STRING)
    private HazardStatus status;

    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "hazardReport",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonIgnore
    @Builder.Default
    private List<Vote> votes = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

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
                .plusHours(24)
                .isBefore(LocalDateTime.now());
    }

    public long getUpvoteCount() {
        return votes.stream()
                .filter(v -> v.getVoteType() == VoteType.UPVOTE)
                .count();
    }

    public long getDownvoteCount() {
        return votes.stream()
                .filter(v -> v.getVoteType() == VoteType.DOWNVOTE)
                .count();
    }

    public int getScore() {
        return (int) (getUpvoteCount() - getDownvoteCount());
    }

    public void updateStatus(HazardStatus newStatus) {
        this.status = newStatus;
    }

}
