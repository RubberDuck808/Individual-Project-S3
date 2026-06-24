package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.BackgroundDTO;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.repository.BackgroundRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.springframework.stereotype.Service;

import static nl.fontys.db3.backend.service.Constants.BACKGROUND_NOT_FOUND_PREFIX;

@Service
public class BackgroundService extends BaseImageAssetService<Background, BackgroundDTO> {

    public BackgroundService(BackgroundRepository repository, StorageUrlService storageUrlService) {
        super(repository, storageUrlService);
    }

    // Convenience aliases used by controllers / UserService
    public java.util.List<BackgroundDTO> getActiveBackgrounds()                                      { return getActive(); }
    public Background getActiveBackgroundByNameOrThrow(String name)                                  { return getActiveByNameOrThrow(name); }
    public java.util.List<BackgroundDTO> getAllBackgrounds()                                          { return getAll(); }
    public BackgroundDTO getBackgroundById(Long id)                                                  { return getById(id); }
    public BackgroundDTO createBackground(String name, String imagePath)                             { return create(name, imagePath); }
    public BackgroundDTO updateBackground(Long id, String name, String imagePath, Boolean active)    { return update(id, name, imagePath, active); }
    public void deactivateBackground(Long id)                                                        { deactivate(id); }
    public void deleteBackground(Long id)                                                            { delete(id); }

    @Override
    protected BackgroundDTO toDTO(Background a) {
        return BackgroundDTO.builder()
                .id(a.getId())
                .name(a.getName())
                .imagePath(a.getImagePath())
                .url(storageUrlService.publicUrlFromPath(a.getImagePath()))
                .active(a.isActive())
                .build();
    }

    @Override
    protected Background buildEntity(String name, String imagePath) {
        return Background.builder().name(name).imagePath(imagePath).active(true).build();
    }

    @Override
    protected String notFoundPrefix() { return BACKGROUND_NOT_FOUND_PREFIX; }

    @Override
    protected String assetTypeName() { return "background"; }
}
