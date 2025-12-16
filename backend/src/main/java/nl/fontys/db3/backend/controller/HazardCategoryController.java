package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hazard-categories")
public class HazardCategoryController {

    private final HazardCategoryRepository categoryRepo;

    public HazardCategoryController(HazardCategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @GetMapping
    public List<HazardCategory> getAllCategories() {
        return categoryRepo.findAll();
    }
}
