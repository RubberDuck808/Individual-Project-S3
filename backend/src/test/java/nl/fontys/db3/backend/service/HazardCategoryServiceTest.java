package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.HazardCategoryDTO;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.mapper.HazardCategoryMapper;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HazardCategoryServiceTest {

    @Mock
    private HazardCategoryRepository categoryRepository;

    @Mock
    private HazardCategoryMapper categoryMapper;

    @InjectMocks
    private HazardCategoryService categoryService;

    private HazardCategory activeCategory;
    private HazardCategory inactiveCategory;
    private HazardCategoryDTO activeCategoryDTO;
    private HazardCategoryDTO inactiveCategoryDTO;

    @BeforeEach
    void setUp() {
        activeCategory = HazardCategory.builder()
                .id(1L)
                .name("Pothole")
                .iconPath("/icons/pothole.png")
                .active(true)
                .build();

        inactiveCategory = HazardCategory.builder()
                .id(2L)
                .name("Construction")
                .iconPath("/icons/construction.png")
                .active(false)
                .build();

        activeCategoryDTO = new HazardCategoryDTO(
                1L,
                "Pothole",
                "/icons/pothole.png",
                true
        );

        inactiveCategoryDTO = new HazardCategoryDTO(
                2L,
                "Construction",
                "/icons/construction.png",
                false
        );
    }

    @Test
    void getAllCategories_success() {
        List<HazardCategory> categories = List.of(activeCategory, inactiveCategory);
        List<HazardCategoryDTO> categoryDTOs = List.of(activeCategoryDTO, inactiveCategoryDTO);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toDTOList(categories)).thenReturn(categoryDTOs);

        List<HazardCategoryDTO> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository).findAll();
        verify(categoryMapper).toDTOList(categories);
    }

    @Test
    void getActiveCategories_success() {
        List<HazardCategory> allCategories = List.of(activeCategory, inactiveCategory);

        when(categoryRepository.findAll()).thenReturn(allCategories);
        when(categoryMapper.toDTO(activeCategory)).thenReturn(activeCategoryDTO);

        List<HazardCategoryDTO> result = categoryService.getActiveCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pothole", result.get(0).name());
        assertTrue(result.get(0).active());
        verify(categoryRepository).findAll();
        verify(categoryMapper).toDTO(activeCategory);
        verify(categoryMapper, never()).toDTO(inactiveCategory);
    }

    @Test
    void getActiveCategories_noActiveCategories() {
        List<HazardCategory> allCategories = List.of(inactiveCategory);

        when(categoryRepository.findAll()).thenReturn(allCategories);

        List<HazardCategoryDTO> result = categoryService.getActiveCategories();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(categoryRepository).findAll();
        verify(categoryMapper, never()).toDTO(any());
    }

    @Test
    void getCategoryById_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(activeCategory));

        HazardCategory result = categoryService.getCategoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Pothole", result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_nullId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.getCategoryById(null);
        });

        assertEquals("Category ID cannot be null", exception.getMessage());
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void getCategoryById_notFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.getCategoryById(999L);
        });

        assertEquals("Category not found: 999", exception.getMessage());
        verify(categoryRepository).findById(999L);
    }

    @Test
    void getCategoryByName_success() {
        List<HazardCategory> categories = List.of(activeCategory, inactiveCategory);

        when(categoryRepository.findAll()).thenReturn(categories);

        HazardCategory result = categoryService.getCategoryByName("Pothole");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Pothole", result.getName());
        verify(categoryRepository).findAll();
    }

    @Test
    void getCategoryByName_caseInsensitive() {
        List<HazardCategory> categories = List.of(activeCategory);

        when(categoryRepository.findAll()).thenReturn(categories);

        HazardCategory result = categoryService.getCategoryByName("pothole");

        assertNotNull(result);
        assertEquals("Pothole", result.getName());
    }

    @Test
    void getCategoryByName_nullName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.getCategoryByName(null);
        });

        assertEquals("Category name cannot be null or blank", exception.getMessage());
        verify(categoryRepository, never()).findAll();
    }

    @Test
    void getCategoryByName_blankName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.getCategoryByName("   ");
        });

        assertEquals("Category name cannot be null or blank", exception.getMessage());
        verify(categoryRepository, never()).findAll();
    }

    @Test
    void getCategoryByName_notFound() {
        List<HazardCategory> categories = List.of(activeCategory);

        when(categoryRepository.findAll()).thenReturn(categories);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.getCategoryByName("Nonexistent");
        });

        assertEquals("Category not found: Nonexistent", exception.getMessage());
        verify(categoryRepository).findAll();
    }
}
