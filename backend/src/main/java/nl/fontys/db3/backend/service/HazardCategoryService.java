package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.HazardCategoryDTO;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.mapper.HazardCategoryMapper;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HazardCategoryService {

    private final HazardCategoryRepository repo;
    private final HazardCategoryMapper mapper;

    public HazardCategoryService(HazardCategoryRepository repo, HazardCategoryMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<HazardCategoryDTO> getAllCategories() {
        return mapper.toDTOList(repo.findAll());
    }

    public Optional<HazardCategoryDTO> getCategoryById(Long id) {
        return repo.findById(id).map(mapper::toDTO);
    }

    public HazardCategoryDTO createCategory(HazardCategoryDTO dto) {
        HazardCategory entity = mapper.toEntity(dto);
        return mapper.toDTO(repo.save(entity));
    }

    public void deleteCategory(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Category not found");
        }
        repo.deleteById(id);
    }
}
