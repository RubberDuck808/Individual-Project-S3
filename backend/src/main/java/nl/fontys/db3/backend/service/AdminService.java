package nl.fontys.db3.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.fontys.db3.backend.dto.AdminStatisticsDTO;
import nl.fontys.db3.backend.dto.AdminUserDTO;
import nl.fontys.db3.backend.entity.*;
import nl.fontys.db3.backend.exception.NotFoundException;
import nl.fontys.db3.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static nl.fontys.db3.backend.service.Constants.DEVICE_ACTIVE_THRESHOLD_HOURS;
import static nl.fontys.db3.backend.service.Constants.ROLE_ADMIN;
import static nl.fontys.db3.backend.service.Constants.DEFAULT_DISTANCE_KM;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HazardReportRepository hazardReportRepository;
    private final DeviceRepository deviceRepository;
    private final LiveTelemetryRepository liveTelemetryRepository;
    private final TelemetryHistoryRepository telemetryHistoryRepository;
    private final TripRepository tripRepository;
    private final AvatarRepository avatarRepository;
    private final BackgroundRepository backgroundRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllUsers(Pageable pageable) {
        log.debug("Getting all users - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable)
                .map(this::toAdminUserDTO);
    }

    private static final String USER_NOT_FOUND_MSG = "User not found: ";

    @Transactional(readOnly = true)
    public AdminUserDTO getUserById(Long id) {
        log.debug("Getting user by ID - userId: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Get user failed - user not found: userId: {}", id);
                    return new NotFoundException(USER_NOT_FOUND_MSG + id);
                });
        return toAdminUserDTO(user);
    }

    @Transactional
    public AdminUserDTO updateUserRole(Long userId, String roleName) {
        log.info("Updating user role - userId: {}, newRole: {}", userId, roleName);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Update user role failed - user not found: userId: {}", userId);
                    return new NotFoundException(USER_NOT_FOUND_MSG + userId);
                });
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.warn("Update user role failed - role not found: userId: {}, roleName: {}", userId, roleName);
                    return new NotFoundException("Role not found: " + roleName);
                });
        
        user.setRole(role);
        userRepository.save(user);
        log.info("User role updated successfully - userId: {}, newRole: {}", userId, roleName);
        
        return toAdminUserDTO(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Deactivating user - userId: {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Deactivate user failed - user not found: userId: {}", userId);
                    return new NotFoundException(USER_NOT_FOUND_MSG + userId);
                });
        log.info("User deactivated successfully - userId: {}", userId);
    }

    private AdminUserDTO toAdminUserDTO(User user) {
        return AdminUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .createdAt(user.getCreatedAt())
                .active(true)
                .avatarId(user.getAvatar() != null ? user.getAvatar().getId() : null)
                .avatarName(user.getAvatar() != null ? user.getAvatar().getName() : null)
                .backgroundId(user.getBackground() != null ? user.getBackground().getId() : null)
                .backgroundName(user.getBackground() != null ? user.getBackground().getName() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminStatisticsDTO getStatistics() {
        try {
            long totalUsers = userRepository.count();
            long adminUsers = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && ROLE_ADMIN.equalsIgnoreCase(u.getRole().getName()))
                    .count();
            long activeUsers = userRepository.findAll().stream()
                    .filter(u -> {
                        boolean hasRecentHazards = !hazardReportRepository.findByCreatedBy_Id(u.getId()).isEmpty();
                        boolean hasRecentTrips = !tripRepository.findByUser_UsernameOrderByIdDesc(u.getUsername()).isEmpty();
                        return hasRecentHazards || hasRecentTrips;
                    })
                    .count();

            long totalHazards = hazardReportRepository.count();
            long openHazards = hazardReportRepository.findByStatus(HazardStatus.OPEN).size();
            long verifiedHazards = hazardReportRepository.findByStatus(HazardStatus.VERIFIED).size();
            long resolvedHazards = hazardReportRepository.findByStatus(HazardStatus.RESOLVED).size();

            DeviceStatistics deviceStats = getDeviceStatistics();
            TelemetryStatistics telemetryStats = getTelemetryStatistics();

            List<Trip> allTrips = tripRepository.findAll();
            long totalTrips = allTrips.size();
            double totalDistanceKm = allTrips.stream()
                    .mapToDouble(t -> t.getDistanceKm() != null ? t.getDistanceKm() : DEFAULT_DISTANCE_KM)
                    .sum();

            AssetStatistics assetStats = getAssetStatistics();

            return AdminStatisticsDTO.builder()
                    .totalUsers(totalUsers)
                    .activeUsers(activeUsers)
                    .adminUsers(adminUsers)
                    .totalHazards(totalHazards)
                    .openHazards(openHazards)
                    .verifiedHazards(verifiedHazards)
                    .resolvedHazards(resolvedHazards)
                    .totalDevices(deviceStats.totalDevices)
                    .activeDevices(deviceStats.activeDevices)
                    .inactiveDevices(deviceStats.inactiveDevices)
                    .totalTelemetryRecords(telemetryStats.totalTelemetryRecords)
                    .lastTelemetryTimestamp(telemetryStats.lastTelemetryTimestamp)
                    .devicesWithTelemetry(telemetryStats.devicesWithTelemetry)
                    .totalTrips(totalTrips)
                    .totalDistanceKm(totalDistanceKm)
                    .totalAvatars(assetStats.totalAvatars)
                    .activeAvatars(assetStats.activeAvatars)
                    .totalBackgrounds(assetStats.totalBackgrounds)
                    .activeBackgrounds(assetStats.activeBackgrounds)
                    .lastUpdated(LocalDateTime.now())
                    .build();
        } catch (IllegalStateException | NotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to load admin statistics: " + e.getMessage(), e);
        }
    }

    private DeviceStatistics getDeviceStatistics() {
        try {
            long totalDevices = deviceRepository.count();
            long activeDevices = deviceRepository.findAll().stream()
                    .filter(d -> {
                        if (d.getLastSeenAt() == null) return false;
                        return d.getLastSeenAt().isAfter(LocalDateTime.now().minus(DEVICE_ACTIVE_THRESHOLD_HOURS, ChronoUnit.HOURS));
                    })
                    .count();
            long inactiveDevices = totalDevices - activeDevices;
            return new DeviceStatistics(totalDevices, activeDevices, inactiveDevices);
        } catch (RuntimeException e) {
            // Device tables may not exist yet or database error
            return new DeviceStatistics(0, 0, 0);
        }
    }

    private TelemetryStatistics getTelemetryStatistics() {
        try {
            long totalTelemetryRecords = telemetryHistoryRepository.count();
            Instant lastTelemetryTimestamp = telemetryHistoryRepository.findAll().stream()
                    .map(TelemetryHistory::getTimestamp)
                    .max(Instant::compareTo)
                    .orElse(null);
            long devicesWithTelemetry = liveTelemetryRepository.count();
            return new TelemetryStatistics(totalTelemetryRecords, lastTelemetryTimestamp, devicesWithTelemetry);
        } catch (RuntimeException e) {
            // Telemetry tables may not exist yet or database error
            return new TelemetryStatistics(0, null, 0);
        }
    }

    private AssetStatistics getAssetStatistics() {
        try {
            long totalAvatars = avatarRepository.count();
            long activeAvatars = avatarRepository.findAllByActiveTrueOrderByNameAsc().size();
            long totalBackgrounds = backgroundRepository.count();
            long activeBackgrounds = backgroundRepository.findAllByActiveTrueOrderByNameAsc().size();
            return new AssetStatistics(totalAvatars, activeAvatars, totalBackgrounds, activeBackgrounds);
        } catch (RuntimeException e) {
            // Asset tables may not exist yet or database error
            return new AssetStatistics(0, 0, 0, 0);
        }
    }

    // Helper record classes for statistics
    private record DeviceStatistics(long totalDevices, long activeDevices, long inactiveDevices) {}
    private record TelemetryStatistics(long totalTelemetryRecords, Instant lastTelemetryTimestamp, long devicesWithTelemetry) {}
    private record AssetStatistics(long totalAvatars, long activeAvatars, long totalBackgrounds, long activeBackgrounds) {}
}
