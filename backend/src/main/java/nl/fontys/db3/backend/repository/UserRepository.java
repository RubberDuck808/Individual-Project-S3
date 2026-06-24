package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    long countByRole_Name(String roleName);

    @Query("SELECT COUNT(DISTINCT u.id) FROM User u WHERE EXISTS " +
           "(SELECT 1 FROM HazardReport h WHERE h.createdBy = u) OR EXISTS " +
           "(SELECT 1 FROM Trip t WHERE t.user = u)")
    long countUsersWithActivity();

    long countByAvatar_Id(Long avatarId);

    long countByBackground_Id(Long backgroundId);

    /**
     * Returns [avatarId, userCount] pairs for all avatars that have at least one user.
     * Used by AdminAssetService to build usage counts in a single query (avoids N+1).
     */
    @Query("SELECT u.avatar.id, COUNT(u) FROM User u WHERE u.avatar IS NOT NULL GROUP BY u.avatar.id")
    List<Object[]> findAvatarUsageCounts();

    /**
     * Returns [backgroundId, userCount] pairs for all backgrounds that have at least one user.
     * Used by AdminAssetService to build usage counts in a single query (avoids N+1).
     */
    @Query("SELECT u.background.id, COUNT(u) FROM User u WHERE u.background IS NOT NULL GROUP BY u.background.id")
    List<Object[]> findBackgroundUsageCounts();
}
