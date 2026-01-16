package nl.fontys.db3.backend.service;

import jakarta.persistence.EntityNotFoundException;
import nl.fontys.db3.backend.dto.AvatarDTO;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.repository.AvatarRepository;
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
class AvatarServiceTest {

    @Mock private AvatarRepository avatarRepository;
    @Mock private StorageUrlService storageUrlService;

    @InjectMocks
    private AvatarService service;

    @Test
    void getActiveAvatars_success() {
        Avatar avatar = mock(Avatar.class);
        when(avatar.getId()).thenReturn(1L);
        when(avatar.getName()).thenReturn("Avatar1");
        when(avatar.getImagePath()).thenReturn("path/to/image.png");
        when(avatar.isActive()).thenReturn(true);
        List<Avatar> avatars = List.of(avatar);
        when(avatarRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(avatars);
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        List<AvatarDTO> result = service.getActiveAvatars();

        assertEquals(1, result.size());
        verify(avatarRepository).findAllByActiveTrueOrderByNameAsc();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getActiveAvatarByNameOrThrow_invalidName_throws(String name) {
        assertThrows(IllegalArgumentException.class,
                () -> service.getActiveAvatarByNameOrThrow(name));
        verifyNoInteractions(avatarRepository);
    }

    @Test
    void getActiveAvatarByNameOrThrow_notFound_throws() {
        when(avatarRepository.findByNameIgnoreCaseAndActiveTrue("avatar1"))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getActiveAvatarByNameOrThrow("avatar1"));
    }

    @Test
    void getActiveAvatarByNameOrThrow_success_returnsAvatar() {
        Avatar avatar = mock(Avatar.class);
        when(avatarRepository.findByNameIgnoreCaseAndActiveTrue("avatar1"))
                .thenReturn(Optional.of(avatar));

        Avatar result = service.getActiveAvatarByNameOrThrow("avatar1");

        assertSame(avatar, result);
        verify(avatarRepository).findByNameIgnoreCaseAndActiveTrue("avatar1");
    }

    @Test
    void getAvatarById_success_returnsDTO() {
        Avatar avatar = mock(Avatar.class);
        when(avatar.getId()).thenReturn(1L);
        when(avatar.getName()).thenReturn("Avatar1");
        when(avatar.getImagePath()).thenReturn("path/to/image.png");
        when(avatar.isActive()).thenReturn(true);
        when(avatarRepository.findById(1L)).thenReturn(Optional.of(avatar));
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        AvatarDTO result = service.getAvatarById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(avatarRepository).findById(1L);
    }

    @Test
    void createAvatar_nameExists_throws() {
        when(avatarRepository.existsByName("Avatar1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.createAvatar("Avatar1", "path/to/image.png"));
        verify(avatarRepository, never()).save(any());
    }

    @Test
    void createAvatar_success_createsAvatar() {
        when(avatarRepository.existsByName("Avatar1")).thenReturn(false);
        when(avatarRepository.existsByImagePath("path/to/image.png")).thenReturn(false);
        when(avatarRepository.save(any(Avatar.class))).thenAnswer(inv -> inv.getArgument(0));
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        AvatarDTO result = service.createAvatar("Avatar1", "path/to/image.png");

        assertNotNull(result);
        ArgumentCaptor<Avatar> captor = ArgumentCaptor.forClass(Avatar.class);
        verify(avatarRepository).save(captor.capture());
        Avatar saved = captor.getValue();
        assertEquals("Avatar1", saved.getName());
        assertEquals("path/to/image.png", saved.getImagePath());
        assertTrue(saved.isActive());
    }

    @Test
    void updateAvatar_notFound_throws() {
        when(avatarRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.updateAvatar(1L, "NewName", null, null));
    }

    @Test
    void updateAvatar_nameExists_throws() {
        Avatar avatar = mock(Avatar.class);
        when(avatar.getName()).thenReturn("OldName");
        when(avatarRepository.findById(1L)).thenReturn(Optional.of(avatar));
        when(avatarRepository.existsByName("NewName")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateAvatar(1L, "NewName", null, null));
        verify(avatarRepository, never()).save(any());
    }

    @Test
    void updateAvatar_success_updatesFields() {
        Avatar avatar = mock(Avatar.class);
        when(avatar.getName()).thenReturn("OldName");
        when(avatar.getImagePath()).thenReturn("old/path.png");
        when(avatar.isActive()).thenReturn(true);
        when(avatarRepository.findById(1L)).thenReturn(Optional.of(avatar));
        when(avatarRepository.existsByName("NewName")).thenReturn(false);
        when(avatarRepository.save(avatar)).thenReturn(avatar);
        when(storageUrlService.publicUrlFromPath(anyString()))
                .thenReturn("http://example.com/image.png");

        AvatarDTO result = service.updateAvatar(1L, "NewName", "new/path.png", false);

        assertNotNull(result);
        verify(avatar).setName("NewName");
        verify(avatar).setImagePath("new/path.png");
        verify(avatar).setActive(false);
        verify(avatarRepository).save(avatar);
    }

    @Test
    void deactivateAvatar_success() {
        Avatar avatar = mock(Avatar.class);
        when(avatarRepository.findById(1L)).thenReturn(Optional.of(avatar));
        when(avatarRepository.save(avatar)).thenReturn(avatar);

        service.deactivateAvatar(1L);

        verify(avatar).setActive(false);
        verify(avatarRepository).save(avatar);
    }

    @Test
    void deleteAvatar_success() {
        Avatar avatar = mock(Avatar.class);
        when(avatarRepository.findById(1L)).thenReturn(Optional.of(avatar));

        service.deleteAvatar(1L);

        verify(avatarRepository).delete(avatar);
    }
}