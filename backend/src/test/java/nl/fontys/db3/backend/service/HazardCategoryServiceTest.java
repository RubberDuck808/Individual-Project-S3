package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.HazardCategoryDTO;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.mapper.HazardCategoryMapper;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HazardCategoryServiceTest {

    @Mock private HazardCategoryRepository categoryRepository;
    @Mock private HazardCategoryMapper categoryMapper;

    @InjectMocks
    private HazardCategoryService service;

    @Test
    void getActiveCategories_filtersActive() {
        HazardCategory active1 = mock(HazardCategory.class);
        when(active1.isActive()).thenReturn(true);

        HazardCategory active2 = mock(HazardCategory.class);
        when(active2.isActive()).thenReturn(true);

        HazardCategory inactive = mock(HazardCategory.class);
        when(inactive.isActive()).thenReturn(false);

        List<HazardCategory> all = List.of(active1, inactive, active2);
        when(categoryRepository.findAll()).thenReturn(all);

        HazardCategoryDTO dto1 = mock(HazardCategoryDTO.class);
        HazardCategoryDTO dto2 = mock(HazardCategoryDTO.class);
        when(categoryMapper.toDTO(active1)).thenReturn(dto1);
        when(categoryMapper.toDTO(active2)).thenReturn(dto2);

        List<HazardCategoryDTO> result = service.getActiveCategories();

        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
        verify(categoryMapper, never()).toDTO(inactive);
    }

    @Test
    void getCategoryById_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getCategoryById(null));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getCategoryById_notFound_throws() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getCategoryById(1L));

        assertEquals("Category not found: 1", ex.getMessage());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_success_returnsCategory() {
        HazardCategory category = mock(HazardCategory.class);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        HazardCategory result = service.getCategoryById(1L);

        assertSame(category, result);
        verify(categoryRepository).findById(1L);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getCategoryByName_invalidName_throws(String name) {
        assertThrows(IllegalArgumentException.class, () -> service.getCategoryByName(name));
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void getCategoryByName_notFound_throws() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getCategoryByName("Unknown"));

        assertEquals("Category not found: Unknown", ex.getMessage());
    }

    @Test
    void getCategoryByName_success_caseInsensitive() {
        HazardCategory category = mock(HazardCategory.class);
        when(category.getName()).thenReturn("Pothole");

        when(categoryRepository.findAll()).thenReturn(List.of(category));

        HazardCategory result = service.getCategoryByName("pothole");

        assertSame(category, result);
    }
}