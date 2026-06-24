package nl.fontys.db3.backend.repository;

import nl.fontys.db3.backend.entity.BaseImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Shared repository interface for BaseImageAsset subtypes (Avatar, Background).
 * Eliminates duplicated method declarations across AvatarRepository and BackgroundRepository.
 */
@NoRepositoryBean
public interface ImageAssetRepository<T extends BaseImageAsset> extends JpaRepository<T, Long> {
    List<T> findAllByActiveTrueOrderByNameAsc();
    long countByActiveTrue();
    boolean existsByImagePath(String imagePath);
    boolean existsByName(String name);
    Optional<T> findByNameIgnoreCaseAndActiveTrue(String name);
}
