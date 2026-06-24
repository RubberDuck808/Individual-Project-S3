package nl.fontys.db3.backend.service;

import jakarta.persistence.EntityNotFoundException;
import nl.fontys.db3.backend.dto.AvatarDTO;
import nl.fontys.db3.backend.entity.Avatar;
import nl.fontys.db3.backend.repository.AvatarRepository;
import nl.fontys.db3.backend.service.storage.StorageUrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock private AvatarRepository avatarRepository;
    @Mock private StorageUrlService storageUrlService;

    private AvatarService avatarService;

    private Avatar avatar;

    @BeforeEach
    void setUp() {
        avatarService = new AvatarService(avatarRepository, storageUrlService);
        avatar = Avatar.builder().name("Robot").imagePath("avatars/robot.png").active(true).build();
        // Fake the id since Lombok/JPA doesn't set it in tests
        avatar.setId(1L);
        when(storageUrlService.publicUrlFromPath(any())).thenReturn("https://cdn/avatars/robot.png");
    }

    @Test
    void getActiveAvatars_returnsMappedList() {
        when(avatarRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(avatar));

        List<AvatarDTO> result = avatarService.getActiveAvatars();

        assertEquals(1, result.size());
        assertEquals("Robot", result.get(0).getName());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void getAvatarById_found() {
        when(avatarRepository.findById(1L)).thenReturn(Optional.of(avatar));

        AvatarDTO dto = avatarService.getAvatarById(1L);

        assertEquals(1L, dto.getId());
        assertEquals("Robot", dto.getName());
    }

    @Test
    void getAvatarById_notFound_throwsEntityNotFoundException() {
        when(avatarRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> avatarService.getAvatarById(99L));
    }

    @Test
    void createAvatar_success() {
        when(avatarRepository.existsByName("Robot")).thenReturn(false);
        when(avatarRepository.existsByImagePath("avatars/robot.png")).thenReturn(false);
        when(avatarRepository.save(any(Avatar.class))).thenReturn(avatar);

        AvatarDTO result = avatarService.createAvatar("Robot", "avatars/robot.png");

        assertNotNull(result);
        assertEquals("Robot", result.getName());
        verify(avatarRepository).save(any(Avatar.class));
    }

    @Test
    void createAvatar_duplicateName_throwsIllegalArgument() {
        when(avatarRepository.existsByName("Robot")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> avatarService.createAvatar("Robot", "avatars/robot.png"));
        verify(avatarRepository, never()).save(any());
    }

    @Test
    void deactivateAvatar_setsActiveFalse() {
        when(avatarRepository.findById(1L)).thenReturn(Optional.of(avatar));
        when(avatarRepository.save(any())).thenReturn(avatar);

        avatarService.deactivateAvatar(1L);

        assertFalse(avatar.isActive());
        verify(avatarRepository).save(avatar);
    }

    @Test
    void deleteAvatar_success() {
        when(avatarRepository.findById(1L)).thenReturn(Optional.of(avatar));

        avatarService.deleteAvatar(1L);

        verify(avatarRepository).delete(avatar);
    }
}
