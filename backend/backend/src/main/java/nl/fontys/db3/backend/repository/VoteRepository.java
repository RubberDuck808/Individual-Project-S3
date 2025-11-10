package nl.fontys.db3.backend.repository;

import org.springframework.stereotype.Repository;
import nl.fontys.db3.backend.entity.Vote;
import nl.fontys.db3.backend.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByHazardReport_Id(Long hazardReportId);

    boolean existsByHazardReport_IdAndUser_Id(Long hazardId, Long userId);

    long countByHazardReport_IdAndVoteType(Long hazardId, VoteType voteType);

}
