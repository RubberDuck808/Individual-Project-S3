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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAssetService {

    private final AvatarRepository avatarRepository;
    private final BackgroundRepository backgroundRepository;
    private final UserRepository userRepository;
    private final StorageUrlService storageUrlService;

    @Transactional(readOnly = true)
    public List<AdminAssetDTO> getAllAvatars() {
        // Single query to fetch all usage counts — avoids N+1
        Map<Long, Long> usageById = buildUsageMap(userRepository.findAvatarUsageCounts());
        return avatarRepository.findAll().stream()
                .map(a -> toAdminAssetDTO(a, usageById.getOrDefault(a.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminAssetDTO> getAllBackgrounds() {
        // Single query to fetch all usage counts — avoids N+1
        Map<Long, Long> usageById = buildUsageMap(userRepository.findBackgroundUsageCounts());
        return backgroundRepository.findAll().stream()
                .map(b -> toAdminAssetDTO(b, usageById.getOrDefault(b.getId(), 0L)))
                .toList();
    }

    /** Converts JPQL [id, count] rows into a Map<id, count>. */
    private Map<Long, Long> buildUsageMap(List<Object[]> rows) {
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private AdminAssetDTO toAdminAssetDTO(Avatar avatar, long usageCount) {
        return AdminAssetDTO.builder()
                .id(avatar.getId())
                .name(avatar.getName())
                .imagePath(avatar.getImagePath())
                .url(storageUrlService.publicUrlFromPath(avatar.getImagePath()))
                .active(avatar.isActive())
                .usageCount(usageCount)
                .build();
    }

    private AdminAssetDTO toAdminAssetDTO(Background background, long usageCount) {
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
