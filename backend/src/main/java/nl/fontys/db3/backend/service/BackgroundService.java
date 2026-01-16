package nl.fontys.db3.backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.BackgroundDTO;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.repository.BackgroundRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static nl.fontys.db3.backend.service.Constants.BACKGROUND_NOT_FOUND_PREFIX;

@Service
@RequiredArgsConstructor
public class BackgroundService {

    private final BackgroundRepository backgroundRepository;
    private final StorageUrlService storageUrlService;

    @Transactional(readOnly = true)
    public List<BackgroundDTO> getActiveBackgrounds() {
        return backgroundRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Background getActiveBackgroundByNameOrThrow(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("backgroundName is required");
        }

        return backgroundRepository.findByNameIgnoreCaseAndActiveTrue(name.trim())
                .orElseThrow(() -> new EntityNotFoundException("Active background not found: " + name));
    }


    @Transactional(readOnly = true)
    public List<BackgroundDTO> getAllBackgrounds() {
        return backgroundRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public BackgroundDTO getBackgroundById(Long id) {
        Background background = backgroundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BACKGROUND_NOT_FOUND_PREFIX + id));
        return toDTO(background);
    }

    @Transactional
    public BackgroundDTO createBackground(String name, String imagePath) {
        if (backgroundRepository.existsByName(name)) {
            throw new IllegalArgumentException("Background with name already exists: " + name);
        }
        if (backgroundRepository.existsByImagePath(imagePath)) {
            throw new IllegalArgumentException("Background with image path already exists: " + imagePath);
        }

        Background background = Background.builder()
                .name(name)
                .imagePath(imagePath)
                .active(true)
                .build();

        Background saved = backgroundRepository.save(background);
        return toDTO(saved);
    }

    @Transactional
    public BackgroundDTO updateBackground(Long id, String name, String imagePath, Boolean active) {
        Background background = backgroundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BACKGROUND_NOT_FOUND_PREFIX + id));

        if (name != null && !name.equals(background.getName())) {
            if (backgroundRepository.existsByName(name)) {
                throw new IllegalArgumentException("Background with name already exists: " + name);
            }
            background.setName(name);
        }

        if (imagePath != null && !imagePath.equals(background.getImagePath())) {
            if (backgroundRepository.existsByImagePath(imagePath)) {
                throw new IllegalArgumentException("Background with image path already exists: " + imagePath);
            }
            background.setImagePath(imagePath);
        }

        if (active != null) {
            background.setActive(active);
        }

        return toDTO(backgroundRepository.save(background));
    }

    @Transactional
    public void deactivateBackground(Long id) {
        Background background = backgroundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BACKGROUND_NOT_FOUND_PREFIX + id));

        background.setActive(false);
        backgroundRepository.save(background);
    }

    @Transactional
    public void deleteBackground(Long id) {
        Background background = backgroundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(BACKGROUND_NOT_FOUND_PREFIX + id));
        
        backgroundRepository.delete(background);
    }

    private BackgroundDTO toDTO(Background a) {
        return BackgroundDTO.builder()
                .id(a.getId())
                .name(a.getName())
                .imagePath(a.getImagePath())
                .url(storageUrlService.publicUrlFromPath(a.getImagePath()))
                .active(a.isActive())
                .build();
    }
}
