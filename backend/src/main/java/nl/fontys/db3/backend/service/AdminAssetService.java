package nl.fontys.db3.backend.service;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.AdminAssetDTO;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.repository.AvatarRepository;
import nl.fontys.db3.backend.repository.BackgroundRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAssetService {

    private final AvatarRepository avatarRepository;
    private final BackgroundRepository backgroundRepository;
    private final UserRepository userRepository;
    private final StorageUrlService storageUrlService;

    @Transactional(readOnly = true)
    public List<AdminAssetDTO> getAllAvatars() {
        return avatarRepository.findAll().stream()
                .map(this::toAdminAssetDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminAssetDTO> getAllBackgrounds() {
        return backgroundRepository.findAll().stream()
                .map(this::toAdminAssetDTO)
                .toList();
    }

    private AdminAssetDTO toAdminAssetDTO(Avatar avatar) {
        // Count how many users are using this avatar
        long usageCount = userRepository.findAll().stream()
                .filter(u -> u.getAvatar() != null && u.getAvatar().getId().equals(avatar.getId()))
                .count();

        return AdminAssetDTO.builder()
                .id(avatar.getId())
                .name(avatar.getName())
                .imagePath(avatar.getImagePath())
                .url(storageUrlService.publicUrlFromPath(avatar.getImagePath()))
                .active(avatar.isActive())
                .usageCount(usageCount)
                .build();
    }

    private AdminAssetDTO toAdminAssetDTO(Background background) {
        // Count how many users are using this background
        long usageCount = userRepository.findAll().stream()
                .filter(u -> u.getBackground() != null && u.getBackground().getId().equals(background.getId()))
                .count();

        return AdminAssetDTO.builder()
                .id(background.getId())
                .name(background.getName())
                .imagePath(background.getImagePath())
                .url(storageUrlService.publicUrlFromPath(background.getImagePath()))
                .active(background.isActive())
                .usageCount(usageCount)
                .build();
    }
}
