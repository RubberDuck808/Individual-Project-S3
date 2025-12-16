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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HazardQueryServiceTest {

    @Mock
    private HazardReportRepository hazardRepo;

    @InjectMocks
    private HazardQueryService service;

    @Test
    void getById_success() {
        HazardReport hazard = mock(HazardReport.class);
        when(hazardRepo.findById(1L)).thenReturn(Optional.of(hazard));

        HazardReport result = service.getById(1L);

        assertNotNull(result);
    }

    @Test
    void getById_nullId() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getById(null));
    }

    @Test
    void getOpenHazards_filtersExpired() {
        HazardReport active = mock(HazardReport.class);
        HazardReport expired = mock(HazardReport.class);

        when(active.isExpired()).thenReturn(false);
        when(expired.isExpired()).thenReturn(true);

        when(hazardRepo.findByStatus(HazardStatus.OPEN))
                .thenReturn(List.of(active, expired));

        List<HazardReport> result = service.getOpenHazards();

        assertEquals(1, result.size());
        assertTrue(result.contains(active));
    }
}
