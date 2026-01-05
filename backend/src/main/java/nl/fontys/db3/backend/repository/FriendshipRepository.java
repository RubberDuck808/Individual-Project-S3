package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Friendship;
import nl.fontys.db3.backend.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Incoming requests for a user
    List<Friendship> findByAddressee_IdAndStatus(Long addresseeId, FriendshipStatus status);

    // Outgoing requests from a user
    List<Friendship> findByRequester_IdAndStatus(Long requesterId, FriendshipStatus status);

    // Accepted friendships involving a user (either side)
    List<Friendship> findByStatusAndRequester_IdOrStatusAndAddressee_Id(
            FriendshipStatus status1, Long requesterId,
            FriendshipStatus status2, Long addresseeId
    );
    @Query("""
        select f from Friendship f
        where (f.requester.id = :userA and f.addressee.id = :userB)
        or (f.requester.id = :userB and f.addressee.id = :userA)
    """)
    Optional<Friendship> findBetweenUsers(
            @Param("userA") Long userA,
            @Param("userB") Long userB
    );

}
