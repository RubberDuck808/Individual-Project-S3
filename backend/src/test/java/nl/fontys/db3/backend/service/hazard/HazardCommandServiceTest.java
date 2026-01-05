package nl.fontys.db3.backend.service.hazard;

import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.dto.HazardReportDTO;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.mapper.HazardMapper;
import nl.fontys.db3.backend.repository.HazardCategoryRepository;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.StatisticsService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HazardCommandServiceTest {

    @Mock private HazardReportRepository hazardRepo;
    @Mock private HazardCategoryRepository categoryRepo;
    @Mock private UserRepository userRepo;
    @Mock private StatisticsService statisticsService;
    @Mock private HazardWsPublisher wsPublisher;
    @Mock private HazardMapper hazardMapper;

    @InjectMocks
    private HazardCommandService service;

    @Test
    void createHazard_success() {
        // Arrange
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(1L);
        when(dto.getLatitude()).thenReturn(51.44);
        when(dto.getLongitude()).thenReturn(5.48);

        String creatorEmail = "user@test.com";

        HazardCategory category = mock(HazardCategory.class);

        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);

        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        when(hazardRepo.save(any(HazardReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HazardReportDTO hazardDto = mock(HazardReportDTO.class);
        when(hazardMapper.toDTO(any(HazardReport.class))).thenReturn(hazardDto);

        // Act
        HazardReport result = service.createHazard(dto, creatorEmail);

        // Assert
        assertNotNull(result);
        assertEquals(HazardStatus.OPEN, result.getStatus());

        verify(hazardRepo).save(any(HazardReport.class));
        verify(statisticsService).incrementHazards(2L);
        verify(wsPublisher).upsert(hazardDto);
    }

    @Test
    void createHazard_nullCategoryId_throws() {
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.createHazard(dto, "user@test.com"));

        verifyNoInteractions(categoryRepo, userRepo, hazardRepo, statisticsService, wsPublisher, hazardMapper);
    }

    @Test
    void createHazard_categoryNotFound() {
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(1L);

        when(categoryRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.createHazard(dto, "user@test.com"));

        verify(userRepo, never()).findByEmail(anyString());
        verify(hazardRepo, never()).save(any());
        verify(statisticsService, never()).incrementHazards(anyLong());
        verify(wsPublisher, never()).upsert(any(HazardReportDTO.class));
    }

    @Test
    void createHazard_userNotFound() {
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(1L);

        HazardCategory category = mock(HazardCategory.class);
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));

        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.createHazard(dto, "user@test.com"));

        verify(hazardRepo, never()).save(any());
        verify(statisticsService, never()).incrementHazards(anyLong());
        verify(wsPublisher, never()).upsert(any(HazardReportDTO.class));
    }

    @Test
    void verifyHazard_success() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.OPEN);

        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));
        when(hazardRepo.save(hazard)).thenReturn(hazard);

        HazardReportDTO hazardDto = mock(HazardReportDTO.class);
        when(hazardMapper.toDTO(hazard)).thenReturn(hazardDto);

        HazardReport result = service.verifyHazard(1L);

        assertNotNull(result);
        verify(hazard).updateStatus(HazardStatus.VERIFIED);
        verify(hazardRepo).save(hazard);
        verify(wsPublisher).upsert(hazardDto);
    }

    @Test
    void verifyHazard_alreadyResolved_throws() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.RESOLVED);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        assertThrows(IllegalStateException.class,
                () -> service.verifyHazard(1L));

        verify(hazard, never()).updateStatus(any());
        verify(hazardRepo, never()).save(any());
        verify(wsPublisher, never()).upsert(any(HazardReportDTO.class));
    }

    @Test
    void verifyHazard_alreadyRejected_throws() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.REJECTED);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        assertThrows(IllegalStateException.class,
                () -> service.verifyHazard(1L));

        verify(hazard, never()).updateStatus(any());
        verify(hazardRepo, never()).save(any());
        verify(wsPublisher, never()).upsert(any(HazardReportDTO.class));
    }

    @Test
    void resolveHazard_success() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.OPEN);

        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));
        when(hazardRepo.save(hazard)).thenReturn(hazard);

        HazardReportDTO hazardDto = mock(HazardReportDTO.class);
        when(hazardMapper.toDTO(hazard)).thenReturn(hazardDto);

        HazardReport result = service.resolveHazard(1L);

        assertNotNull(result);
        verify(hazard).updateStatus(HazardStatus.RESOLVED);
        verify(hazardRepo).save(hazard);
        verify(wsPublisher).upsert(hazardDto);
    }

    @Test
    void resolveHazard_alreadyResolved_throws() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.RESOLVED);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        assertThrows(IllegalStateException.class,
                () -> service.resolveHazard(1L));

        verify(hazard, never()).updateStatus(any());
        verify(hazardRepo, never()).save(any());
        verify(wsPublisher, never()).upsert(any(HazardReportDTO.class));
    }

    @Test
    void rejectHazard_success() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.OPEN);

        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));
        when(hazardRepo.save(hazard)).thenReturn(hazard);

        HazardReportDTO hazardDto = mock(HazardReportDTO.class);
        when(hazardMapper.toDTO(hazard)).thenReturn(hazardDto);

        HazardReport result = service.rejectHazard(1L);

        assertNotNull(result);
        verify(hazard).updateStatus(HazardStatus.REJECTED);
        verify(hazardRepo).save(hazard);
        verify(wsPublisher).upsert(hazardDto);
    }

    @Test
    void rejectHazard_resolvedHazardThrows() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazard.getStatus()).thenReturn(HazardStatus.RESOLVED);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        assertThrows(IllegalStateException.class,
                () -> service.rejectHazard(1L));

        verify(hazard, never()).updateStatus(any());
        verify(hazardRepo, never()).save(any());
        verify(wsPublisher, never()).upsert(any(HazardReportDTO.class));
    }

    @Test
    void verifyHazard_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.verifyHazard(null));
        verifyNoInteractions(hazardRepo, wsPublisher, hazardMapper);
    }

    @Test
    void resolveHazard_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.resolveHazard(null));
        verifyNoInteractions(hazardRepo, wsPublisher, hazardMapper);
    }

    @Test
    void rejectHazard_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.rejectHazard(null));
        verifyNoInteractions(hazardRepo, wsPublisher, hazardMapper);
    }
}
