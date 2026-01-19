package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.dto.HazardReportDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.mapper.HazardMapper;
import nl.fontys.db3.backend.repository.HazardReportRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.service.hazard.HazardWsPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HazardServiceTest {

    @Mock
    private HazardReportRepository hazardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HazardCategoryService categoryService;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private HazardWsPublisher wsPublisher;

    @Mock
    private HazardMapper hazardMapper;

    @InjectMocks
    private HazardService hazardService;

    private User testUser;
    private Role userRole;
    private HazardCategory category;
    private HazardReport hazard;
    private HazardCreateRequestDTO createDto;

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, "USER");
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .name("Test User")
                .password("encoded")
                .role(userRole)
                .build();

        category = HazardCategory.builder()
                .id(1L)
                .name("Pothole")
                .iconPath("/icons/pothole.png")
                .active(true)
                .build();

        hazard = HazardReport.builder()
                .id(1L)
                .latitude(52.0)
                .longitude(5.0)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        createDto = new HazardCreateRequestDTO();
        createDto.setCategoryId(1L);
        createDto.setLatitude(52.0);
        createDto.setLongitude(5.0);
    }

    @Test
    void createHazard_success() {
        // Given
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(hazardRepository.save(any(HazardReport.class))).thenAnswer(invocation -> {
            HazardReport h = invocation.getArgument(0);
            h.setId(1L);
            return h;
        });
        HazardReportDTO hazardDTO = new HazardReportDTO();
        hazardDTO.setId(1L);
        when(hazardMapper.toDTO(any(HazardReport.class))).thenReturn(hazardDTO);

        // When
        HazardReport result = hazardService.createHazard(createDto, "test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(HazardStatus.OPEN, result.getStatus());
        verify(categoryService).getCategoryById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(hazardRepository).save(any(HazardReport.class));
        verify(statisticsService).incrementHazards(1L);
        verify(wsPublisher).upsert(any(HazardReportDTO.class));
        verify(hazardMapper).toDTO(any(HazardReport.class));
    }

    @Test
    void createHazard_categoryIdNull_throwsException() {
        // Given
        createDto.setCategoryId(null);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.createHazard(createDto, "test@example.com"));
        assertEquals("Category ID cannot be null", exception.getMessage());
        verify(categoryService, never()).getCategoryById(any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void createHazard_userNotFound_throwsException() {
        // Given
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.createHazard(createDto, "test@example.com"));
        assertEquals("User not found", exception.getMessage());
        verify(categoryService).getCategoryById(1L);
        verify(userRepository).findByEmail("test@example.com");
        verify(hazardRepository, never()).save(any());
    }

    @Test
    void verifyHazard_success() {
        // Given
        when(hazardRepository.findById(1L)).thenReturn(Optional.of(hazard));
        when(hazardRepository.save(any(HazardReport.class))).thenReturn(hazard);
        HazardReportDTO hazardDTO = new HazardReportDTO();
        hazardDTO.setId(1L);
        when(hazardMapper.toDTO(any(HazardReport.class))).thenReturn(hazardDTO);

        // When
        HazardReport result = hazardService.verifyHazard(1L);

        // Then
        assertNotNull(result);
        assertEquals(HazardStatus.VERIFIED, result.getStatus());
        verify(hazardRepository).findById(1L);
        verify(hazardRepository).save(hazard);
        verify(wsPublisher).upsert(any(HazardReportDTO.class));
    }

    @Test
    void verifyHazard_resolvedStatus_throwsException() {
        // Given
        hazard.setStatus(HazardStatus.RESOLVED);
        when(hazardRepository.findById(1L)).thenReturn(Optional.of(hazard));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> hazardService.verifyHazard(1L));
        assertEquals("Cannot verify a resolved/rejected hazard.", exception.getMessage());
        verify(hazardRepository).findById(1L);
        verify(hazardRepository, never()).save(any());
    }

    @Test
    void verifyHazard_rejectedStatus_throwsException() {
        // Given
        hazard.setStatus(HazardStatus.REJECTED);
        when(hazardRepository.findById(1L)).thenReturn(Optional.of(hazard));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> hazardService.verifyHazard(1L));
        assertEquals("Cannot verify a resolved/rejected hazard.", exception.getMessage());
    }

    @Test
    void verifyHazard_nullId_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.verifyHazard(null));
        assertEquals("Hazard ID cannot be null", exception.getMessage());
        verify(hazardRepository, never()).findById(any());
    }

    @Test
    void verifyHazard_notFound_throwsException() {
        // Given
        when(hazardRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.verifyHazard(1L));
        assertEquals("Hazard not found", exception.getMessage());
    }

    @Test
    void resolveHazard_success() {
        // Given
        when(hazardRepository.findById(1L)).thenReturn(Optional.of(hazard));
        when(hazardRepository.save(any(HazardReport.class))).thenReturn(hazard);
        HazardReportDTO hazardDTO = new HazardReportDTO();
        hazardDTO.setId(1L);
        when(hazardMapper.toDTO(any(HazardReport.class))).thenReturn(hazardDTO);

        // When
        HazardReport result = hazardService.resolveHazard(1L);

        // Then
        assertNotNull(result);
        assertEquals(HazardStatus.RESOLVED, result.getStatus());
        verify(hazardRepository).findById(1L);
        verify(hazardRepository).save(hazard);
    }

    @Test
    void resolveHazard_alreadyResolved_throwsException() {
        // Given
        hazard.setStatus(HazardStatus.RESOLVED);
        when(hazardRepository.findById(1L)).thenReturn(Optional.of(hazard));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> hazardService.resolveHazard(1L));
        assertEquals("Hazard already resolved.", exception.getMessage());
        verify(hazardRepository).findById(1L);
        verify(hazardRepository, never()).save(any());
    }

    @Test
    void resolveHazard_nullId_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.resolveHazard(null));
        assertEquals("Hazard ID cannot be null", exception.getMessage());
    }

    @Test
    void resolveHazard_notFound_throwsException() {
        // Given
        when(hazardRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.resolveHazard(1L));
        assertEquals("Hazard not found", exception.getMessage());
    }

    @Test
    void rejectHazard_success() {
        // Given
        when(hazardRepository.findById(1L)).thenReturn(Optional.of(hazard));
        when(hazardRepository.save(any(HazardReport.class))).thenReturn(hazard);
        HazardReportDTO hazardDTO = new HazardReportDTO();
        hazardDTO.setId(1L);
        when(hazardMapper.toDTO(any(HazardReport.class))).thenReturn(hazardDTO);

        // When
        HazardReport result = hazardService.rejectHazard(1L);

        // Then
        assertNotNull(result);
        assertEquals(HazardStatus.REJECTED, result.getStatus());
        verify(hazardRepository).findById(1L);
        verify(hazardRepository).save(hazard);
    }

    @Test
    void rejectHazard_resolvedStatus_throwsException() {
        // Given
        hazard.setStatus(HazardStatus.RESOLVED);
        when(hazardRepository.findById(1L)).thenReturn(Optional.of(hazard));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> hazardService.rejectHazard(1L));
        assertEquals("Resolved hazards cannot be rejected.", exception.getMessage());
        verify(hazardRepository).findById(1L);
        verify(hazardRepository, never()).save(any());
    }

    @Test
    void rejectHazard_nullId_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.rejectHazard(null));
        assertEquals("Hazard ID cannot be null", exception.getMessage());
    }

    @Test
    void rejectHazard_notFound_throwsException() {
        // Given
        when(hazardRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.rejectHazard(1L));
        assertEquals("Hazard not found", exception.getMessage());
    }

    @Test
    void getOpenHazards_success() {
        // Given
        HazardReport expiredHazard = HazardReport.builder()
                .id(2L)
                .latitude(52.0)
                .longitude(5.0)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.OPEN)
                .createdAt(LocalDateTime.now().minusDays(31)) // Expired
                .build();

        when(hazardRepository.findByStatus(HazardStatus.OPEN))
                .thenReturn(List.of(hazard, expiredHazard));

        // When
        List<HazardReport> result = hazardService.getOpenHazards();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only non-expired
        assertEquals(1L, result.get(0).getId());
        verify(hazardRepository).findByStatus(HazardStatus.OPEN);
    }

    @Test
    void getActiveHazards_success() {
        // Given
        HazardReport verifiedHazard = HazardReport.builder()
                .id(2L)
                .latitude(52.0)
                .longitude(5.0)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.VERIFIED)
                .createdAt(LocalDateTime.now())
                .build();

        when(hazardRepository.findByStatusIn(anyList()))
                .thenReturn(List.of(hazard, verifiedHazard));

        // When
        List<HazardReport> result = hazardService.getActiveHazards();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(hazardRepository).findByStatusIn(anyList());
    }

    @Test
    void getById_success() {
        // Given
        when(hazardRepository.findById(1L)).thenReturn(Optional.of(hazard));

        // When
        HazardReport result = hazardService.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(hazardRepository).findById(1L);
    }

    @Test
    void getById_nullId_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.getById(null));
        assertEquals("Hazard ID cannot be null", exception.getMessage());
        verify(hazardRepository, never()).findById(any());
    }

    @Test
    void getById_notFound_throwsException() {
        // Given
        when(hazardRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.getById(1L));
        assertEquals("Hazard not found", exception.getMessage());
    }

    @Test
    void getHazardsByUsername_success() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        when(hazardRepository.findByCreatedByUsernameOrderByIdDesc("testuser"))
                .thenReturn(List.of(hazard));

        // When
        List<HazardReport> result = hazardService.getHazardsByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).existsByUsername("testuser");
        verify(hazardRepository).findByCreatedByUsernameOrderByIdDesc("testuser");
    }

    @Test
    void getHazardsByUsername_nullUsername_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.getHazardsByUsername(null));
        assertEquals("Username cannot be null/blank", exception.getMessage());
        verify(userRepository, never()).existsByUsername(any());
    }

    @Test
    void getHazardsByUsername_blankUsername_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.getHazardsByUsername("   "));
        assertEquals("Username cannot be null/blank", exception.getMessage());
    }

    @Test
    void getHazardsByUsername_userNotFound_throwsException() {
        // Given
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.getHazardsByUsername("nonexistent"));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).existsByUsername("nonexistent");
        verify(hazardRepository, never()).findByCreatedByUsernameOrderByIdDesc(any());
    }

    @Test
    void getActiveHazardsByUsername_success() {
        // Given
        HazardReport expiredHazard = HazardReport.builder()
                .id(2L)
                .latitude(52.0)
                .longitude(5.0)
                .category(category)
                .createdBy(testUser)
                .status(HazardStatus.OPEN)
                .createdAt(LocalDateTime.now().minusDays(31)) // Expired
                .build();

        when(hazardRepository.findByCreatedByUsernameOrderByIdDesc("testuser"))
                .thenReturn(List.of(hazard, expiredHazard));

        // When
        List<HazardReport> result = hazardService.getActiveHazardsByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only non-expired
        assertEquals(1L, result.get(0).getId());
        verify(hazardRepository).findByCreatedByUsernameOrderByIdDesc("testuser");
    }

    @Test
    void getActiveHazardsByUsername_nullUsername_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.getActiveHazardsByUsername(null));
        assertEquals("Username cannot be null/blank", exception.getMessage());
    }

    @Test
    void getActiveHazardsByUsername_blankUsername_throwsException() {
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> hazardService.getActiveHazardsByUsername("   "));
        assertEquals("Username cannot be null/blank", exception.getMessage());
    }
}
