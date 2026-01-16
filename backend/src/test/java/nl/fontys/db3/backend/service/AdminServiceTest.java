package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.AdminStatisticsDTO;
import nl.fontys.db3.backend.dto.AdminUserDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.repository.*;

import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private HazardReportRepository hazardReportRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceOwnershipRepository deviceOwnershipRepository;

    @Mock
    private LiveTelemetryRepository liveTelemetryRepository;

    @Mock
    private TelemetryHistoryRepository telemetryHistoryRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private AvatarRepository avatarRepository;

    @Mock
    private BackgroundRepository backgroundRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getAllUsers_success_returnsPage() {
        User user = User.builder().id(1L).email("test@test.com").build();
        Page<User> userPage = new PageImpl<>(List.of(user));
        Pageable pageable = mock(Pageable.class);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<AdminUserDTO> result = adminService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUserById_success_returnsAdminUserDTO() {
        User user = User.builder().id(1L).email("test@test.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserDTO result = adminService.getUserById(1L);

        assertNotNull(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(nl.fontys.db3.backend.exception.NotFoundException.class, 
                () -> adminService.getUserById(1L));
        verify(userRepository).findById(1L);
    }

    @Test
    void getStatistics_success_returnsAdminStatisticsDTO() {
        // Setup users
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        Role userRole = Role.builder().id(2L).name("USER").build();
        User adminUser = User.builder().id(1L).username("admin").role(adminRole).build();
        User regularUser = User.builder().id(2L).username("user").role(userRole).build();
        
        when(userRepository.count()).thenReturn(2L);
        when(userRepository.findAll()).thenReturn(List.of(adminUser, regularUser));
        
        // Setup hazards
        HazardReport openHazard = HazardReport.builder().id(1L).status(HazardStatus.OPEN).build();
        HazardReport verifiedHazard = HazardReport.builder().id(2L).status(HazardStatus.VERIFIED).build();
        HazardReport resolvedHazard = HazardReport.builder().id(3L).status(HazardStatus.RESOLVED).build();
        
        when(hazardReportRepository.count()).thenReturn(3L);
        when(hazardReportRepository.findByStatus(HazardStatus.OPEN)).thenReturn(List.of(openHazard));
        when(hazardReportRepository.findByStatus(HazardStatus.VERIFIED)).thenReturn(List.of(verifiedHazard));
        when(hazardReportRepository.findByStatus(HazardStatus.RESOLVED)).thenReturn(List.of(resolvedHazard));
        when(hazardReportRepository.findByCreatedBy_Id(2L)).thenReturn(List.of(openHazard));
        when(hazardReportRepository.findByCreatedBy_Id(1L)).thenReturn(List.of());
        
        // Setup devices
        Device activeDevice = Device.builder()
                .id(1L)
                .deviceId("DEVICE-1")
                .lastSeenAt(java.time.LocalDateTime.now())
                .build();
        Device inactiveDevice = Device.builder()
                .id(2L)
                .deviceId("DEVICE-2")
                .lastSeenAt(java.time.LocalDateTime.now().minusHours(25))
                .build();
        
        when(deviceRepository.count()).thenReturn(2L);
        when(deviceRepository.findAll()).thenReturn(List.of(activeDevice, inactiveDevice));
        
        // Setup trips
        Trip trip1 = Trip.builder().id(1L).distanceKm(10.5).build();
        Trip trip2 = Trip.builder().id(2L).distanceKm(20.0).build();
        Trip trip3 = Trip.builder().id(3L).distanceKm(null).build(); // null distance
        
        when(tripRepository.findAll()).thenReturn(List.of(trip1, trip2, trip3));
        when(tripRepository.findByUser_UsernameOrderByIdDesc("user")).thenReturn(List.of(trip1));
        when(tripRepository.findByUser_UsernameOrderByIdDesc("admin")).thenReturn(List.of());
        
        // Setup telemetry
        TelemetryHistory history1 = TelemetryHistory.builder()
                .id(1L)
                .timestamp(java.time.Instant.now())
                .build();
        TelemetryHistory history2 = TelemetryHistory.builder()
                .id(2L)
                .timestamp(java.time.Instant.now().minusSeconds(100))
                .build();
        
        when(telemetryHistoryRepository.count()).thenReturn(2L);
        when(telemetryHistoryRepository.findAll()).thenReturn(List.of(history1, history2));
        when(liveTelemetryRepository.count()).thenReturn(1L);
        
        // Setup assets
        Avatar avatar1 = Avatar.builder().id(1L).active(true).build();
        
        when(avatarRepository.count()).thenReturn(2L);
        when(avatarRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(avatar1));
        
        Background bg1 = Background.builder().id(1L).active(true).build();
        
        when(backgroundRepository.count()).thenReturn(2L);
        when(backgroundRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(bg1));

        AdminStatisticsDTO result = adminService.getStatistics();

        assertNotNull(result);
        assertEquals(2L, result.getTotalUsers());
        assertEquals(1L, result.getAdminUsers());
        assertEquals(1L, result.getActiveUsers()); // user has hazards
        assertEquals(3L, result.getTotalHazards());
        assertEquals(1L, result.getOpenHazards());
        assertEquals(1L, result.getVerifiedHazards());
        assertEquals(1L, result.getResolvedHazards());
        assertEquals(2L, result.getTotalDevices());
        assertEquals(1L, result.getActiveDevices());
        assertEquals(1L, result.getInactiveDevices());
        assertEquals(2L, result.getTotalTelemetryRecords());
        assertNotNull(result.getLastTelemetryTimestamp());
        assertEquals(1L, result.getDevicesWithTelemetry());
        assertEquals(3L, result.getTotalTrips());
        assertEquals(30.5, result.getTotalDistanceKm()); // 10.5 + 20.0 + 0.0 (default)
        assertEquals(2L, result.getTotalAvatars());
        assertEquals(1L, result.getActiveAvatars());
        assertEquals(2L, result.getTotalBackgrounds());
        assertEquals(1L, result.getActiveBackgrounds());
        assertNotNull(result.getLastUpdated());
    }

    @Test
    void getStatistics_deviceRepositoryException_handlesGracefully() {
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of());
        when(hazardReportRepository.count()).thenReturn(0L);
        when(hazardReportRepository.findByStatus(any())).thenReturn(List.of());
        when(hazardReportRepository.findByCreatedBy_Id(anyLong())).thenReturn(List.of());
        when(deviceRepository.count()).thenThrow(new RuntimeException("Database error"));
        when(deviceRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        when(tripRepository.findAll()).thenReturn(List.of());
        when(tripRepository.findByUser_UsernameOrderByIdDesc(anyString())).thenReturn(List.of());
        when(telemetryHistoryRepository.count()).thenReturn(0L);
        when(telemetryHistoryRepository.findAll()).thenReturn(List.of());
        when(liveTelemetryRepository.count()).thenReturn(0L);
        when(avatarRepository.count()).thenReturn(0L);
        when(avatarRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());
        when(backgroundRepository.count()).thenReturn(0L);
        when(backgroundRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());

        AdminStatisticsDTO result = adminService.getStatistics();

        assertNotNull(result);
        assertEquals(0L, result.getTotalDevices()); // Should default to 0 on error
        assertEquals(0L, result.getActiveDevices());
        assertEquals(0L, result.getInactiveDevices());
    }

    @Test
    void getStatistics_telemetryRepositoryException_handlesGracefully() {
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of());
        when(hazardReportRepository.count()).thenReturn(0L);
        when(hazardReportRepository.findByStatus(any())).thenReturn(List.of());
        when(hazardReportRepository.findByCreatedBy_Id(anyLong())).thenReturn(List.of());
        when(deviceRepository.count()).thenReturn(0L);
        when(deviceRepository.findAll()).thenReturn(List.of());
        when(tripRepository.findAll()).thenReturn(List.of());
        when(tripRepository.findByUser_UsernameOrderByIdDesc(anyString())).thenReturn(List.of());
        when(telemetryHistoryRepository.count()).thenThrow(new RuntimeException("Database error"));
        when(telemetryHistoryRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        when(liveTelemetryRepository.count()).thenReturn(0L);
        when(avatarRepository.count()).thenReturn(0L);
        when(avatarRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());
        when(backgroundRepository.count()).thenReturn(0L);
        when(backgroundRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());

        AdminStatisticsDTO result = adminService.getStatistics();

        assertNotNull(result);
        assertEquals(0L, result.getTotalTelemetryRecords()); // Should default to 0 on error
        assertNull(result.getLastTelemetryTimestamp());
        assertEquals(0L, result.getDevicesWithTelemetry());
    }

    @Test
    void getStatistics_assetRepositoryException_handlesGracefully() {
        when(userRepository.count()).thenReturn(1L);
        when(userRepository.findAll()).thenReturn(List.of());
        when(hazardReportRepository.count()).thenReturn(0L);
        when(hazardReportRepository.findByStatus(any())).thenReturn(List.of());
        when(hazardReportRepository.findByCreatedBy_Id(anyLong())).thenReturn(List.of());
        when(deviceRepository.count()).thenReturn(0L);
        when(deviceRepository.findAll()).thenReturn(List.of());
        when(tripRepository.findAll()).thenReturn(List.of());
        when(tripRepository.findByUser_UsernameOrderByIdDesc(anyString())).thenReturn(List.of());
        when(telemetryHistoryRepository.count()).thenReturn(0L);
        when(telemetryHistoryRepository.findAll()).thenReturn(List.of());
        when(liveTelemetryRepository.count()).thenReturn(0L);
        when(avatarRepository.count()).thenThrow(new RuntimeException("Database error"));
        when(avatarRepository.findAllByActiveTrueOrderByNameAsc()).thenThrow(new RuntimeException("Database error"));
        when(backgroundRepository.count()).thenReturn(0L);
        when(backgroundRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of());

        AdminStatisticsDTO result = adminService.getStatistics();

        assertNotNull(result);
        assertEquals(0L, result.getTotalAvatars()); // Should default to 0 on error
        assertEquals(0L, result.getActiveAvatars());
        assertEquals(0L, result.getTotalBackgrounds());
        assertEquals(0L, result.getActiveBackgrounds());
    }

    @Test
    void updateUserRole_success_updatesRole() {
        User user = User.builder().id(1L).email("test@test.com").username("testuser").name("Test").build();
        Role adminRole = Role.builder().id(2L).name("ADMIN").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(user)).thenReturn(user);

        AdminUserDTO result = adminService.updateUserRole(1L, "ADMIN");

        assertNotNull(result);
        assertEquals("ADMIN", result.getRoleName());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRole_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(nl.fontys.db3.backend.exception.NotFoundException.class, 
                () -> adminService.updateUserRole(1L, "ADMIN"));
        verify(userRepository).findById(1L);
        verify(roleRepository, never()).findByName(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserRole_roleNotFound_throws() {
        User user = User.builder().id(1L).email("test@test.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("INVALID")).thenReturn(Optional.empty());

        assertThrows(nl.fontys.db3.backend.exception.NotFoundException.class, 
                () -> adminService.updateUserRole(1L, "INVALID"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateUser_success_verifiesUserExists() {
        User user = User.builder().id(1L).email("test@test.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.deactivateUser(1L);

        verify(userRepository).findById(1L);
    }

    @Test
    void deactivateUser_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(nl.fontys.db3.backend.exception.NotFoundException.class, 
                () -> adminService.deactivateUser(1L));
        verify(userRepository).findById(1L);
    }


    @Test
    void getUserById_withAvatarAndBackground_mapsCorrectly() {
        Avatar avatar = Avatar.builder().id(1L).name("avatar1").build();
        Background background = Background.builder().id(2L).name("bg1").build();
        Role role = Role.builder().id(3L).name("USER").build();
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .username("testuser")
                .name("Test User")
                .avatar(avatar)
                .background(background)
                .role(role)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserDTO result = adminService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getAvatarId());
        assertEquals("avatar1", result.getAvatarName());
        assertEquals(2L, result.getBackgroundId());
        assertEquals("bg1", result.getBackgroundName());
        assertEquals("USER", result.getRoleName());
    }

    @Test
    void getUserById_withNullRole_mapsCorrectly() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .username("testuser")
                .name("Test User")
                .role(null)
                .avatar(null)
                .background(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserDTO result = adminService.getUserById(1L);

        assertNotNull(result);
        assertNull(result.getRoleName());
        assertNull(result.getAvatarId());
        assertNull(result.getAvatarName());
        assertNull(result.getBackgroundId());
        assertNull(result.getBackgroundName());
    }

    @Test
    void getAllUsers_emptyPage_returnsEmpty() {
        Page<User> emptyPage = new PageImpl<>(List.of());
        Pageable pageable = mock(Pageable.class);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<AdminUserDTO> result = adminService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        verify(userRepository).findAll(pageable);
    }
}
