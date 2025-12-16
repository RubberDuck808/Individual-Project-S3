package nl.fontys.db3.backend.service.hazard;

import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HazardCommandServiceTest {

    @Mock
    private HazardReportRepository hazardRepo;

    @Mock
    private HazardCategoryRepository categoryRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private HazardCommandService service;

    @Test
    void createHazard_success() {
        // Arrange
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(1L);
        when(dto.getCreatedBy()).thenReturn(2L);
        when(dto.getLatitude()).thenReturn(51.44);
        when(dto.getLongitude()).thenReturn(5.48);

        HazardCategory category = mock(HazardCategory.class);
        User user = mock(User.class);

        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(userRepo.findById(2L)).thenReturn(Optional.of(user));

        when(hazardRepo.save(any(HazardReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        HazardReport result = service.createHazard(dto);

        // Assert
        assertNotNull(result);
        assertEquals(HazardStatus.OPEN, result.getStatus());
        verify(hazardRepo).save(any(HazardReport.class));
    }

    @Test
    void createHazard_categoryNotFound() {
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(1L);
        when(dto.getCreatedBy()).thenReturn(2L);

        when(categoryRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.createHazard(dto));
    }

    @Test
    void verifyHazard_success() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.OPEN);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));
        when(hazardRepo.save(hazard)).thenReturn(hazard);

        HazardReport result = service.verifyHazard(1L);

        assertNotNull(result);
        verify(hazard).updateStatus(HazardStatus.VERIFIED);
    }

    @Test
    void verifyHazard_alreadyResolved() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.RESOLVED);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        assertThrows(IllegalStateException.class,
                () -> service.verifyHazard(1L));
    }

    @Test
    void resolveHazard_success() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.OPEN);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));
        when(hazardRepo.save(hazard)).thenReturn(hazard);

        service.resolveHazard(1L);

        verify(hazard).updateStatus(HazardStatus.RESOLVED);
    }

    @Test
    void rejectHazard_resolvedHazardThrows() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.RESOLVED);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        assertThrows(IllegalStateException.class,
                () -> service.rejectHazard(1L));
    }
}
