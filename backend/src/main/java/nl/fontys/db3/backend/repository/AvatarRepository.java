package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    List<Avatar> findAllByActiveTrueOrderByNameAsc();
    boolean existsByImagePath(String imagePath);
    boolean existsByName(String name);
    Optional<Avatar> findByNameIgnoreCaseAndActiveTrue(String name);
}
