package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Table(
    name = "app_user",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
    }
)
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "avatar_id")
    private Avatar avatar;

    @ManyToOne
    @JoinColumn(name = "background_id")
    private Background background;


    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "requester")
    @ToString.Exclude
    @Builder.Default
    private List<Friendship> sentFriendRequests = new ArrayList<>();

    @OneToMany(mappedBy = "addressee")
    @ToString.Exclude
    @Builder.Default
    private List<Friendship> receivedFriendRequests = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    @Builder.Default
    private List<FavouriteLocation> favouriteLocations = new ArrayList<>();
}
