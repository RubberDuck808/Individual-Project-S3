package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.dto.HazardReportDTO;
import nl.fontys.db3.backend.entity.HazardCategory;
import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.mapper.HazardMapper;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.hazard.HazardWsPublisher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HazardServiceTest {

    @Mock private HazardReportRepository hazardRepo;
    @Mock private UserRepository userRepo;
    @Mock private HazardCategoryService categoryService;
    @Mock private StatisticsService statisticsService;
    @Mock private HazardWsPublisher wsPublisher;
    @Mock private HazardMapper hazardMapper;

    @InjectMocks
    private HazardService service;

    @Test
    void createHazard_success() {
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(1L);
        when(dto.getLatitude()).thenReturn(51.44);
        when(dto.getLongitude()).thenReturn(5.48);

        String creatorEmail = "user@test.com";

        HazardCategory category = mock(HazardCategory.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);

        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        when(hazardRepo.save(any(HazardReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HazardReportDTO hazardDto = mock(HazardReportDTO.class);
        when(hazardMapper.toDTO(any(HazardReport.class))).thenReturn(hazardDto);

        HazardReport result = service.createHazard(dto, creatorEmail);

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

        verifyNoInteractions(categoryService, userRepo, hazardRepo, statisticsService, wsPublisher, hazardMapper);
    }

    @Test
    void createHazard_categoryNotFound_throws() {
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(1L);

        when(categoryService.getCategoryById(1L)).thenThrow(new IllegalArgumentException("Category not found: 1"));

        assertThrows(IllegalArgumentException.class,
                () -> service.createHazard(dto, "user@test.com"));

        verify(userRepo, never()).findByEmail(anyString());
        verify(hazardRepo, never()).save(any());
    }

    @Test
    void createHazard_userNotFound_throws() {
        HazardCreateRequestDTO dto = mock(HazardCreateRequestDTO.class);
        when(dto.getCategoryId()).thenReturn(1L);

        HazardCategory category = mock(HazardCategory.class);
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.createHazard(dto, "user@test.com"));

        verify(hazardRepo, never()).save(any());
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
    void getOpenHazards_filtersExpired() {
        HazardReport active = mock(HazardReport.class);
        when(active.isExpired()).thenReturn(false);

        HazardReport expired = mock(HazardReport.class);
        when(expired.isExpired()).thenReturn(true);

        when(hazardRepo.findByStatus(HazardStatus.OPEN)).thenReturn(List.of(active, expired));

        List<HazardReport> result = service.getOpenHazards();

        assertEquals(1, result.size());
        assertSame(active, result.get(0));
        verify(hazardRepo).findByStatus(HazardStatus.OPEN);
    }

    @Test
    void getActiveHazards_filtersExpired() {
        HazardReport active1 = mock(HazardReport.class);
        when(active1.isExpired()).thenReturn(false);

        HazardReport active2 = mock(HazardReport.class);
        when(active2.isExpired()).thenReturn(false);

        HazardReport expired = mock(HazardReport.class);
        when(expired.isExpired()).thenReturn(true);

        when(hazardRepo.findByStatusIn(anyList())).thenReturn(List.of(active1, expired, active2));

        List<HazardReport> result = service.getActiveHazards();

        assertEquals(2, result.size());
        assertTrue(result.contains(active1));
        assertTrue(result.contains(active2));

        verify(hazardRepo).findByStatusIn(List.of(HazardStatus.OPEN, HazardStatus.VERIFIED));
    }

    @Test
    void getById_notFound_throws() {
        when(hazardRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.getById(1L));
        verify(hazardRepo).findById(1L);
    }

    @Test
    void getById_success_returnsEntity() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        HazardReport result = service.getById(1L);

        assertSame(hazard, result);
        verify(hazardRepo).findById(1L);
    }

    @Test
    void getActiveHazardsByUsername_filtersExpired() {
        HazardReport active = mock(HazardReport.class);
        when(active.isExpired()).thenReturn(false);

        HazardReport expired = mock(HazardReport.class);
        when(expired.isExpired()).thenReturn(true);

        when(hazardRepo.findByCreatedByUsernameOrderByIdDesc("bob"))
                .thenReturn(List.of(expired, active));

        List<HazardReport> result = service.getActiveHazardsByUsername("bob");

        assertEquals(1, result.size());
        assertSame(active, result.get(0));
        verify(hazardRepo).findByCreatedByUsernameOrderByIdDesc("bob");
    }
}