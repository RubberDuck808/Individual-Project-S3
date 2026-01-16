package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.AdminAssetDTO;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.entity.Background;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.AvatarRepository;
import nl.fontys.db3.backend.repository.BackgroundRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAssetServiceTest {

    @Mock private AvatarRepository avatarRepository;
    @Mock private BackgroundRepository backgroundRepository;
    @Mock private UserRepository userRepository;
    @Mock private StorageUrlService storageUrlService;

    @InjectMocks
    private AdminAssetService service;

    @Test
    void getAllAvatars_delegatesToRepository() {
        Avatar avatar = mock(Avatar.class);
        when(avatar.getId()).thenReturn(1L);
        when(avatar.getName()).thenReturn("Avatar1");
        when(avatar.getImagePath()).thenReturn("path/to/image.png");
        when(avatar.isActive()).thenReturn(true);
        when(avatarRepository.findAll()).thenReturn(List.of(avatar));
        when(userRepository.findAll()).thenReturn(List.of());
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        List<AdminAssetDTO> result = service.getAllAvatars();

        assertEquals(1, result.size());
        verify(avatarRepository).findAll();
    }

    @Test
    void getAllAvatars_countsUsage() {
        Avatar avatar = mock(Avatar.class);
        when(avatar.getId()).thenReturn(1L);
        when(avatar.getName()).thenReturn("Avatar1");
        when(avatar.getImagePath()).thenReturn("path/to/image.png");
        when(avatar.isActive()).thenReturn(true);

        User user1 = mock(User.class);
        when(user1.getAvatar()).thenReturn(avatar);
        User user2 = mock(User.class);
        when(user2.getAvatar()).thenReturn(avatar);
        User user3 = mock(User.class);
        when(user3.getAvatar()).thenReturn(null);

        when(avatarRepository.findAll()).thenReturn(List.of(avatar));
        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        List<AdminAssetDTO> result = service.getAllAvatars();

        assertEquals(1, result.size());
        AdminAssetDTO dto = result.get(0);
        assertEquals(2L, dto.getUsageCount());
    }

    @Test
    void getAllBackgrounds_delegatesToRepository() {
        Background background = mock(Background.class);
        when(background.getId()).thenReturn(1L);
        when(background.getName()).thenReturn("Bg1");
        when(background.getImagePath()).thenReturn("path/to/image.png");
        when(background.isActive()).thenReturn(true);
        when(backgroundRepository.findAll()).thenReturn(List.of(background));
        when(userRepository.findAll()).thenReturn(List.of());
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        List<AdminAssetDTO> result = service.getAllBackgrounds();

        assertEquals(1, result.size());
        verify(backgroundRepository).findAll();
    }

    @Test
    void getAllBackgrounds_countsUsage() {
        Background background = mock(Background.class);
        when(background.getId()).thenReturn(1L);
        when(background.getName()).thenReturn("Bg1");
        when(background.getImagePath()).thenReturn("path/to/image.png");
        when(background.isActive()).thenReturn(true);

        User user1 = mock(User.class);
        when(user1.getBackground()).thenReturn(background);
        User user2 = mock(User.class);
        when(user2.getBackground()).thenReturn(null);

        when(backgroundRepository.findAll()).thenReturn(List.of(background));
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(storageUrlService.publicUrlFromPath("path/to/image.png"))
                .thenReturn("http://example.com/image.png");

        List<AdminAssetDTO> result = service.getAllBackgrounds();

        assertEquals(1, result.size());
        AdminAssetDTO dto = result.get(0);
        assertEquals(1L, dto.getUsageCount());
    }

}
