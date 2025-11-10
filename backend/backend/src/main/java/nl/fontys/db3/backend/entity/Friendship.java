package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User requester;

    @ManyToOne
    private User addressee;

    @Enumerated(EnumType.STRING)
    private FriendshipStatus status; // REQUESTED, ACCEPTED, DECLINED, BLOCKED

    private LocalDateTime createdAt;
}

