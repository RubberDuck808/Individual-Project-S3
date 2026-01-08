package nl.fontys.db3.backend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.AvatarDTO;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.repository.AvatarRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final AvatarRepository avatarRepository;
    private final StorageUrlService storageUrlService;

    @Transactional(readOnly = true)
    public List<AvatarDTO> getActiveAvatars() {
        return avatarRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Avatar getActiveAvatarByNameOrThrow(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("avatarName is required");
        }

        return avatarRepository.findByNameIgnoreCaseAndActiveTrue(name.trim())
                .orElseThrow(() -> new EntityNotFoundException("Active avatar not found: " + name));
    }


    @Transactional
    public void deactivateAvatar(Long id) {
        Avatar avatar = avatarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avatar not found: " + id));

        avatar.setActive(false);
    }

    private AvatarDTO toDTO(Avatar a) {
        return AvatarDTO.builder()
                .name(a.getName())
                .imagePath(a.getImagePath())
                .url(storageUrlService.publicUrlFromPath(a.getImagePath()))
                .active(a.isActive())
                .build();
    }
}
