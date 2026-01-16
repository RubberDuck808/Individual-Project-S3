package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(
  name = "vote",
  uniqueConstraints = @UniqueConstraint(columnNames = {"hazard_report_id", "user_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hazard_report_id", nullable = false)
    @JsonIgnore
    private HazardReport hazardReport;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
