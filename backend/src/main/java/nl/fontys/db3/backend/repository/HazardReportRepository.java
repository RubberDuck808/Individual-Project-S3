package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HazardReportRepository extends JpaRepository<HazardReport, Long> {

    // Hazards reported by a specific user
    List<HazardReport> findByCreatedBy_Id(Long userId);

    // Hazards filtered by category name
    List<HazardReport> findByCategory_Name(String categoryName);

    // Hazards matching exact status
    List<HazardReport> findByStatus(HazardStatus status);

    long countByStatus(HazardStatus status);

    // Hazards matching multiple statuses
    List<HazardReport> findByStatusIn(List<HazardStatus> statuses);

    // Hazards created after a specific time
    List<HazardReport> findByCreatedAtAfter(LocalDateTime since);

    List<HazardReport> findByCreatedByUsernameOrderByIdDesc(String username);

    // Eagerly fetch votes to avoid N+1 when filtering by expiry
    @EntityGraph(attributePaths = "votes")
    @Query("SELECT h FROM HazardReport h WHERE h.status = :status")
    List<HazardReport> findByStatusWithVotes(HazardStatus status);

    @EntityGraph(attributePaths = "votes")
    @Query("SELECT h FROM HazardReport h WHERE h.status IN :statuses")
    List<HazardReport> findByStatusInWithVotes(List<HazardStatus> statuses);

    @EntityGraph(attributePaths = "votes")
    @Query("SELECT h FROM HazardReport h WHERE h.createdBy.username = :username ORDER BY h.id DESC")
    List<HazardReport> findByCreatedByUsernameWithVotes(String username);

}
