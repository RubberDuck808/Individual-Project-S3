package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Background;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BackgroundRepository extends JpaRepository<Background, Long> {
    List<Background> findAllByActiveTrueOrderByNameAsc();
    boolean existsByImagePath(String imagePath);
    boolean existsByName(String name);
    Optional<Background> findByNameIgnoreCaseAndActiveTrue(String name);
}
