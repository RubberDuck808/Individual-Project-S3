package nl.fontys.db3.backend.repository;

import org.springframework.stereotype.Repository;
import nl.fontys.db3.backend.entity.Vote;
import nl.fontys.db3.backend.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // all votes for a hazard
    List<Vote> findByHazardReport_Id(Long hazardReportId);

    // check if a user already voted on a hazard
    boolean existsByHazardReport_IdAndUser_Id(Long hazardId, Long userId);

    // count upvotes/downvotes
    long countByHazardReport_IdAndType(Long hazardId, VoteType type);
}
