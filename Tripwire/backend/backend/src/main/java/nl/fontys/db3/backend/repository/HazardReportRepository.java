package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface HazardReportRepository extends JpaRepository<HazardReport, Long> {

    // find all hazards created by a specific user
    List<HazardReport> findByCreatedBy_Id(Long userId);

    // find hazards in a category
    List<HazardReport> findByCategory_Name(String categoryName);

    // find hazards still open (not resolved, not expired)
    List<HazardReport> findByStatus(HazardStatus status);

    // optional: hazards created after certain time
    List<HazardReport> findByCreatedAtAfter(LocalDateTime since);
}

