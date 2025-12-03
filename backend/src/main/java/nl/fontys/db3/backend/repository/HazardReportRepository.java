package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // Hazards matching multiple statuses (🚀 needed for ACTIVE: OPEN + VERIFIED)
    List<HazardReport> findByStatusIn(List<HazardStatus> statuses);

    // Hazards created after a specific time (optional analytics)
    List<HazardReport> findByCreatedAtAfter(LocalDateTime since);
}
