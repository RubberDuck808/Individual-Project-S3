package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Statistics;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    Optional<Statistics> findByUser_Id(Long userId);

    Optional<Statistics> findByUser_Username(String username);

    boolean existsByUser_Id(Long userId);

    // Atomic increments (safe under concurrency)
    @Modifying(clearAutomatically = true)
    @Query("update Statistics s set s.totalVotes = s.totalVotes + 1 where s.user.id = :userId")
    int incVotes(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update Statistics s set s.totalHazardsReported = s.totalHazardsReported + 1 where s.user.id = :userId")
    int incHazards(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
        @Query("""
        update Statistics s
        set s.totalTrips = s.totalTrips + 1,
            s.totalDistanceKm = s.totalDistanceKm + :km
        where s.user.id = :userId
        """)
    int incTripsAndAddDistance(@Param("userId") Long userId, @Param("km") double km);

}
