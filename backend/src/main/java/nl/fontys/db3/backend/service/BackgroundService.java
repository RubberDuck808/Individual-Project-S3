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


    @Transactional
    public void deactivateBackground(Long id) {
        Background background = backgroundRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Background not found: " + id));

        background.setActive(false);
    }

    private BackgroundDTO toDTO(Background a) {
        return BackgroundDTO.builder()
                .name(a.getName())
                .imagePath(a.getImagePath())
                .url(storageUrlService.publicUrlFromPath(a.getImagePath()))
                .active(a.isActive())
                .build();
    }
}
