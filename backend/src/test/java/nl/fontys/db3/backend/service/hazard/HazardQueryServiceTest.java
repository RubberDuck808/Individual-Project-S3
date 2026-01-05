package nl.fontys.db3.backend.service.hazard;

import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.HazardStatus;
import nl.fontys.db3.backend.repository.HazardReportRepository;

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
class HazardQueryServiceTest {

    @Mock
    private HazardReportRepository hazardRepo;

    @InjectMocks
    private HazardQueryService service;

    @Test
    void getOpenHazards_filtersExpired() {
        // Arrange
        HazardReport active = mock(HazardReport.class);
        when(active.isExpired()).thenReturn(false);

        HazardReport expired = mock(HazardReport.class);
        when(expired.isExpired()).thenReturn(true);

        when(hazardRepo.findByStatus(HazardStatus.OPEN)).thenReturn(List.of(active, expired));

        // Act
        List<HazardReport> result = service.getOpenHazards();

        // Assert
        assertEquals(1, result.size());
        assertSame(active, result.get(0));
        verify(hazardRepo).findByStatus(HazardStatus.OPEN);
    }

    @Test
    void getActiveHazards_filtersExpired_andUsesStatusIn() {
        // Arrange
        HazardReport active1 = mock(HazardReport.class);
        when(active1.isExpired()).thenReturn(false);

        HazardReport active2 = mock(HazardReport.class);
        when(active2.isExpired()).thenReturn(false);

        HazardReport expired = mock(HazardReport.class);
        when(expired.isExpired()).thenReturn(true);

        when(hazardRepo.findByStatusIn(anyList())).thenReturn(List.of(active1, expired, active2));

        // Act
        List<HazardReport> result = service.getActiveHazards();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(active1));
        assertTrue(result.contains(active2));

        // Verify it called repo with OPEN + VERIFIED
        verify(hazardRepo).findByStatusIn(List.of(HazardStatus.OPEN, HazardStatus.VERIFIED));
    }

    @Test
    void getById_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getById(null));
        verifyNoInteractions(hazardRepo);
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
    void getHazardsByUsername_nullOrBlank_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getHazardsByUsername(null));
        assertThrows(IllegalArgumentException.class, () -> service.getHazardsByUsername("  "));
        verifyNoInteractions(hazardRepo);
    }

    @Test
    void getHazardsByUsername_delegatesToRepo() {
        List<HazardReport> hazards = List.of(mock(HazardReport.class), mock(HazardReport.class));
        when(hazardRepo.findByCreatedByUsernameOrderByIdDesc("bob")).thenReturn(hazards);

        List<HazardReport> result = service.getHazardsByUsername("bob");

        assertSame(hazards, result);
        verify(hazardRepo).findByCreatedByUsernameOrderByIdDesc("bob");
    }

    @Test
    void getActiveHazardsByUsername_nullOrBlank_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.getActiveHazardsByUsername(null));
        assertThrows(IllegalArgumentException.class, () -> service.getActiveHazardsByUsername(" "));
        verifyNoInteractions(hazardRepo);
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
