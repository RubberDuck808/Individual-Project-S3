package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.AdminStatisticsDTO;
import nl.fontys.db3.backend.dto.AdminUserDTO;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.exception.NotFoundException;
import nl.fontys.db3.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private HazardReportRepository hazardReportRepository;
    @Mock private DeviceRepository deviceRepository;
    @Mock private LiveTelemetryRepository liveTelemetryRepository;
    @Mock private TelemetryHistoryRepository telemetryHistoryRepository;
    @Mock private TripRepository tripRepository;
    @Mock private AvatarRepository avatarRepository;
    @Mock private BackgroundRepository backgroundRepository;

    @InjectMocks
    private AdminService adminService;

    private User alice;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("USER").build();
        adminRole = Role.builder().id(2L).name("ADMIN").build();
        alice = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@test.com")
                .name("Alice")
                .password("encoded")
                .role(userRole)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllUsers_returnsMappedPage() {
        Page<User> page = new PageImpl<>(List.of(alice));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<AdminUserDTO> result = adminService.getAllUsers(PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("alice", result.getContent().get(0).getUsername());
        assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    void getUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        AdminUserDTO dto = adminService.getUserById(1L);

        assertEquals(1L, dto.getId());
        assertEquals("alice", dto.getUsername());
    }

    @Test
    void getUserById_notFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> adminService.getUserById(99L));
    }

    @Test
    void updateUserRole_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(alice);

        AdminUserDTO result = adminService.updateUserRole(1L, "ADMIN");

        verify(userRepository).save(alice);
        assertNotNull(result);
    }

    @Test
    void updateUserRole_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> adminService.updateUserRole(99L, "ADMIN"));
    }

    @Test
    void updateUserRole_roleNotFound_throwsNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(roleRepository.findByName("SUPERADMIN")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> adminService.updateUserRole(1L, "SUPERADMIN"));
    }

    @Test
    void deactivateUser_setsActiveFalseAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.save(any(User.class))).thenReturn(alice);

        adminService.deactivateUser(1L);

        assertFalse(alice.isActive());
        verify(userRepository).save(alice);
    }

    @Test
    void deactivateUser_notFound_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> adminService.deactivateUser(99L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getStatistics_usesEfficientQueries() {
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByRole_Name("ADMIN")).thenReturn(1L);
        when(userRepository.countUsersWithActivity()).thenReturn(3L);
        when(hazardReportRepository.count()).thenReturn(10L);
        when(hazardReportRepository.countByStatus(any())).thenReturn(4L);
        when(deviceRepository.count()).thenReturn(2L);
        when(deviceRepository.countByLastSeenAtAfter(any())).thenReturn(1L);
        when(telemetryHistoryRepository.count()).thenReturn(100L);
        when(telemetryHistoryRepository.findTopByOrderByTimestampDesc()).thenReturn(null);
        when(liveTelemetryRepository.count()).thenReturn(1L);
        when(tripRepository.count()).thenReturn(7L);
        when(tripRepository.sumTotalDistanceKm()).thenReturn(42.5);
        when(avatarRepository.count()).thenReturn(3L);
        when(avatarRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());
        when(backgroundRepository.count()).thenReturn(2L);
        when(backgroundRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());

        AdminStatisticsDTO stats = adminService.getStatistics();

        assertEquals(5L, stats.getTotalUsers());
        assertEquals(1L, stats.getAdminUsers());
        assertEquals(3L, stats.getActiveUsers());
        assertEquals(10L, stats.getTotalHazards());
        assertEquals(7L, stats.getTotalTrips());
        assertEquals(42.5, stats.getTotalDistanceKm());

        // Verify no findAll() calls — no N+1 queries
        verify(userRepository, never()).findAll();
        verify(hazardReportRepository, never()).findAll();
        verify(tripRepository, never()).findAll();
    }
}
