package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.HazardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HazardCategoryRepository extends JpaRepository<HazardCategory, Long> {
}
