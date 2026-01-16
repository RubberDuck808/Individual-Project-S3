package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.HazardCategoryDTO;
import nl.fontys.db3.backend.service.HazardCategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hazard-categories")
public class HazardCategoryController {

    private final HazardCategoryService categoryService;

    public HazardCategoryController(HazardCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<HazardCategoryDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }
}
