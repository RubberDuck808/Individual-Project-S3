package nl.fontys.db3.backend.repository;

import org.springframework.stereotype.Repository;
import nl.fontys.db3.backend.entity.Vote;
import nl.fontys.db3.backend.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByHazardReport_Id(Long hazardReportId);

    boolean existsByHazardReport_IdAndUser_Id(Long hazardId, Long userId);

    long countByHazardReport_IdAndVoteType(Long hazardId, VoteType voteType);

    Optional<Vote> findByHazardReport_IdAndUser_Id(Long hazardId, Long userId);

    @Query("""
    select count(v)
    from Vote v
    where v.user.username = :username
    """)
    long countVotesCastByUser(@Param("username") String username);

}
