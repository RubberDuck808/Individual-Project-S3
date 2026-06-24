package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.AvatarDTO;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.repository.AvatarRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.springframework.stereotype.Service;

import static nl.fontys.db3.backend.service.Constants.AVATAR_NOT_FOUND_PREFIX;

@Service
public class AvatarService extends BaseImageAssetService<Avatar, AvatarDTO> {

    public AvatarService(AvatarRepository repository, StorageUrlService storageUrlService) {
        super(repository, storageUrlService);
    }

    // Convenience aliases used by controllers / UserService
    public java.util.List<AvatarDTO> getActiveAvatars()                               { return getActive(); }
    public Avatar getActiveAvatarByNameOrThrow(String name)                            { return getActiveByNameOrThrow(name); }
    public java.util.List<AvatarDTO> getAllAvatars()                                   { return getAll(); }
    public AvatarDTO getAvatarById(Long id)                                            { return getById(id); }
    public AvatarDTO createAvatar(String name, String imagePath)                       { return create(name, imagePath); }
    public AvatarDTO updateAvatar(Long id, String name, String imagePath, Boolean active) { return update(id, name, imagePath, active); }
    public void deactivateAvatar(Long id)                                              { deactivate(id); }
    public void deleteAvatar(Long id)                                                  { delete(id); }

    @Override
    protected AvatarDTO toDTO(Avatar a) {
        return AvatarDTO.builder()
                .id(a.getId())
                .name(a.getName())
                .imagePath(a.getImagePath())
                .url(storageUrlService.publicUrlFromPath(a.getImagePath()))
                .active(a.isActive())
                .build();
    }

    @Override
    protected Avatar buildEntity(String name, String imagePath) {
        return Avatar.builder().name(name).imagePath(imagePath).active(true).build();
    }

    @Override
    protected String notFoundPrefix() { return AVATAR_NOT_FOUND_PREFIX; }

    @Override
    protected String assetTypeName() { return "avatar"; }
}
