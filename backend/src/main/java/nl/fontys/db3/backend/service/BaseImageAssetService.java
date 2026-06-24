package nl.fontys.db3.backend.service;

import jakarta.persistence.EntityNotFoundException;
import nl.fontys.db3.backend.entity.BaseImageAsset;
import nl.fontys.db3.backend.repository.ImageAssetRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Shared logic for image-asset services (AvatarService, BackgroundService).
 * Eliminates ~100 lines of duplicated CRUD code between the two services.
 */
public abstract class BaseImageAssetService<T extends BaseImageAsset, D> {

    protected final ImageAssetRepository<T> repository;
    protected final StorageUrlService storageUrlService;

    protected BaseImageAssetService(ImageAssetRepository<T> repository,
                                    StorageUrlService storageUrlService) {
        this.repository = repository;
        this.storageUrlService = storageUrlService;
    }

    protected abstract D toDTO(T entity);
    protected abstract T buildEntity(String name, String imagePath);
    protected abstract String notFoundPrefix();
    protected abstract String assetTypeName();

    @Transactional(readOnly = true)
    public List<D> getActive() {
        return repository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public T getActiveByNameOrThrow(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(assetTypeName() + "Name is required");
        }
        return repository.findByNameIgnoreCaseAndActiveTrue(name.trim())
                .orElseThrow(() -> new EntityNotFoundException("Active " + assetTypeName() + " not found: " + name));
    }

    @Transactional(readOnly = true)
    public List<D> getAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public D getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Transactional
    public D create(String name, String imagePath) {
        if (repository.existsByName(name)) {
            throw new IllegalArgumentException(assetTypeName() + " with name already exists: " + name);
        }
        if (repository.existsByImagePath(imagePath)) {
            throw new IllegalArgumentException(assetTypeName() + " with image path already exists: " + imagePath);
        }
        return toDTO(repository.save(buildEntity(name, imagePath)));
    }

    @Transactional
    public D update(Long id, String name, String imagePath, Boolean active) {
        T asset = findOrThrow(id);

        if (name != null && !name.equals(asset.getName())) {
            if (repository.existsByName(name)) {
                throw new IllegalArgumentException(assetTypeName() + " with name already exists: " + name);
            }
            asset.setName(name);
        }

        if (imagePath != null && !imagePath.equals(asset.getImagePath())) {
            if (repository.existsByImagePath(imagePath)) {
                throw new IllegalArgumentException(assetTypeName() + " with image path already exists: " + imagePath);
            }
            asset.setImagePath(imagePath);
        }

        if (active != null) {
            asset.setActive(active);
        }

        return toDTO(repository.save(asset));
    }

    @Transactional
    public void deactivate(Long id) {
        T asset = findOrThrow(id);
        asset.setActive(false);
        repository.save(asset);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findOrThrow(id));
    }

    private T findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(notFoundPrefix() + id));
    }
}
