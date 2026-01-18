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

import static nl.fontys.db3.backend.service.Constants.AVATAR_NOT_FOUND_PREFIX;

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


    @Transactional(readOnly = true)
    public List<AvatarDTO> getAllAvatars() {
        return avatarRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvatarDTO getAvatarById(Long id) {
        Avatar avatar = avatarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AVATAR_NOT_FOUND_PREFIX + id));
        return toDTO(avatar);
    }

    @Transactional
    public AvatarDTO createAvatar(String name, String imagePath) {
        if (avatarRepository.existsByName(name)) {
            throw new IllegalArgumentException("Avatar with name already exists: " + name);
        }
        if (avatarRepository.existsByImagePath(imagePath)) {
            throw new IllegalArgumentException("Avatar with image path already exists: " + imagePath);
        }

        Avatar avatar = Avatar.builder()
                .name(name)
                .imagePath(imagePath)
                .active(true)
                .build();

        Avatar saved = avatarRepository.save(avatar);
        return toDTO(saved);
    }

    @Transactional
    public AvatarDTO updateAvatar(Long id, String name, String imagePath, Boolean active) {
        Avatar avatar = avatarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AVATAR_NOT_FOUND_PREFIX + id));

        if (name != null && !name.equals(avatar.getName())) {
            if (avatarRepository.existsByName(name)) {
                throw new IllegalArgumentException("Avatar with name already exists: " + name);
            }
            avatar.setName(name);
        }

        if (imagePath != null && !imagePath.equals(avatar.getImagePath())) {
            if (avatarRepository.existsByImagePath(imagePath)) {
                throw new IllegalArgumentException("Avatar with image path already exists: " + imagePath);
            }
            avatar.setImagePath(imagePath);
        }

        if (active != null) {
            avatar.setActive(active);
        }

        return toDTO(avatarRepository.save(avatar));
    }

    @Transactional
    public void deactivateAvatar(Long id) {
        Avatar avatar = avatarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AVATAR_NOT_FOUND_PREFIX + id));

        avatar.setActive(false);
        avatarRepository.save(avatar);
    }

    @Transactional
    public void deleteAvatar(Long id) {
        Avatar avatar = avatarRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(AVATAR_NOT_FOUND_PREFIX + id));
        
        avatarRepository.delete(avatar);
    }

    private AvatarDTO toDTO(Avatar a) {
        return AvatarDTO.builder()
                .id(a.getId())
                .name(a.getName())
                .imagePath(a.getImagePath())
                .url(storageUrlService.publicUrlFromPath(a.getImagePath()))
                .active(a.isActive())
                .build();
    }
}
