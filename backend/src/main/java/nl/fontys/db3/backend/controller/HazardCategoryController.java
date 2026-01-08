package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.HazardCategoryDTO;
import nl.fontys.db3.backend.mapper.HazardCategoryMapper;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hazard-categories")
public class HazardCategoryController {

    private final HazardCategoryRepository categoryRepo;
    private final HazardCategoryMapper mapper;

    public HazardCategoryController(HazardCategoryRepository categoryRepo,
                                    HazardCategoryMapper mapper) {
        this.categoryRepo = categoryRepo;
        this.mapper = mapper;
    }

    @GetMapping
    public List<HazardCategoryDTO> getAllCategories() {
        return mapper.toDTOList(categoryRepo.findAll());
    }
}
