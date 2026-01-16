package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.HazardCategoryDTO;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.mapper.HazardCategoryMapper;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HazardCategoryService {

    private final HazardCategoryRepository categoryRepository;
    private final HazardCategoryMapper categoryMapper;

    public HazardCategoryService(HazardCategoryRepository categoryRepository,
                                HazardCategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Get all hazard categories
     */
    @Transactional(readOnly = true)
    public List<HazardCategoryDTO> getAllCategories() {
        return categoryMapper.toDTOList(categoryRepository.findAll());
    }

    /**
     * Get all active hazard categories
     */
    @Transactional(readOnly = true)
    public List<HazardCategoryDTO> getActiveCategories() {
        return categoryRepository.findAll().stream()
                .filter(HazardCategory::isActive)
                .map(categoryMapper::toDTO)
                .toList();
    }

    /**
     * Get category by ID
     */
    @Transactional(readOnly = true)
    public HazardCategory getCategoryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    }

    /**
     * Get category by name
     */
    @Transactional(readOnly = true)
    public HazardCategory getCategoryByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be null or blank");
        }
        return categoryRepository.findAll().stream()
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + name));
    }
}
