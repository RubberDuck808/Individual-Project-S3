package nl.fontys.db3.backend.service;

import jakarta.persistence.EntityNotFoundException;
import nl.fontys.db3.backend.dto.BackgroundDTO;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.repository.BackgroundRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackgroundServiceTest {

    @Mock private BackgroundRepository backgroundRepository;
    @Mock private StorageUrlService storageUrlService;

    @InjectMocks
    private BackgroundService service;

    @Test
    void getActiveBackgrounds_success() {
        Background background = mock(Background.class);
        when(background.getId()).thenReturn(1L);
        when(background.getName()).thenReturn("Bg1");
        when(background.getImagePath()).thenReturn("path/to/image.png");
        when(background.isActive()).thenReturn(true);
        List<Background> backgrounds = List.of(background);
        when(backgroundRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(backgrounds);
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        List<BackgroundDTO> result = service.getActiveBackgrounds();

        assertEquals(1, result.size());
        verify(backgroundRepository).findAllByActiveTrueOrderByNameAsc();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getActiveBackgroundByNameOrThrow_invalidName_throws(String name) {
        assertThrows(IllegalArgumentException.class,
                () -> service.getActiveBackgroundByNameOrThrow(name));
        verifyNoInteractions(backgroundRepository);
    }

    @Test
    void getActiveBackgroundByNameOrThrow_notFound_throws() {
        when(backgroundRepository.findByNameIgnoreCaseAndActiveTrue("bg1"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getActiveBackgroundByNameOrThrow("bg1"));
    }

    @Test
    void getActiveBackgroundByNameOrThrow_success_returnsBackground() {
        Background background = mock(Background.class);
        when(backgroundRepository.findByNameIgnoreCaseAndActiveTrue("bg1"))
                .thenReturn(Optional.of(background));

        Background result = service.getActiveBackgroundByNameOrThrow("bg1");

        assertSame(background, result);
        verify(backgroundRepository).findByNameIgnoreCaseAndActiveTrue("bg1");
    }

    @Test
    void getBackgroundById_success_returnsDTO() {
        Background background = mock(Background.class);
        when(background.getId()).thenReturn(1L);
        when(background.getName()).thenReturn("Bg1");
        when(background.getImagePath()).thenReturn("path/to/image.png");
        when(background.isActive()).thenReturn(true);
        when(backgroundRepository.findById(1L)).thenReturn(Optional.of(background));
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        BackgroundDTO result = service.getBackgroundById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(backgroundRepository).findById(1L);
    }

    @Test
    void createBackground_nameExists_throws() {
        when(backgroundRepository.existsByName("Bg1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.createBackground("Bg1", "path/to/image.png"));
        verify(backgroundRepository, never()).save(any());
    }

    @Test
    void createBackground_success_createsBackground() {
        when(backgroundRepository.existsByName("Bg1")).thenReturn(false);
        when(backgroundRepository.existsByImagePath("path/to/image.png")).thenReturn(false);
        when(backgroundRepository.save(any(Background.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        BackgroundDTO result = service.createBackground("Bg1", "path/to/image.png");

        assertNotNull(result);
        ArgumentCaptor<Background> captor = ArgumentCaptor.forClass(Background.class);
        verify(backgroundRepository).save(captor.capture());
        Background saved = captor.getValue();
        assertEquals("Bg1", saved.getName());
        assertEquals("path/to/image.png", saved.getImagePath());
        assertTrue(saved.isActive());
    }

    @Test
    void updateBackground_notFound_throws() {
        when(backgroundRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateBackground(1L, "NewName", null, null));
    }

    @Test
    void updateBackground_nameExists_throws() {
        Background background = mock(Background.class);
        when(background.getName()).thenReturn("OldName");
        when(backgroundRepository.findById(1L)).thenReturn(Optional.of(background));
        when(backgroundRepository.existsByName("NewName")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateBackground(1L, "NewName", null, null));
        verify(backgroundRepository, never()).save(any());
    }

    @Test
    void updateBackground_success_updatesFields() {
        Background background = mock(Background.class);
        when(background.getName()).thenReturn("OldName");
        when(background.getImagePath()).thenReturn("old/path.png");
        when(background.isActive()).thenReturn(true);
        when(backgroundRepository.findById(1L)).thenReturn(Optional.of(background));
        when(backgroundRepository.existsByName("NewName")).thenReturn(false);
        when(backgroundRepository.save(background)).thenReturn(background);
        when(storageUrlService.publicUrlFromPath(anyString()))
                .thenReturn("http://example.com/image.png");

        BackgroundDTO result = service.updateBackground(1L, "NewName", "new/path.png", false);

        assertNotNull(result);
        verify(background).setName("NewName");
        verify(background).setImagePath("new/path.png");
        verify(background).setActive(false);
        verify(backgroundRepository).save(background);
    }

    @Test
    void deactivateBackground_success() {
        Background background = mock(Background.class);
        when(backgroundRepository.findById(1L)).thenReturn(Optional.of(background));
        when(backgroundRepository.save(background)).thenReturn(background);

        service.deactivateBackground(1L);

        verify(background).setActive(false);
        verify(backgroundRepository).save(background);
    }

    @Test
    void deleteBackground_success() {
        Background background = mock(Background.class);
        when(backgroundRepository.findById(1L)).thenReturn(Optional.of(background));

        service.deleteBackground(1L);

        verify(backgroundRepository).delete(background);
    }
}