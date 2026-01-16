package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.dto.AdminDeviceDTO;
import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.entity.DeviceOwnership;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.DeviceOwnershipRepository;
import nl.fontys.db3.backend.repository.DeviceRepository;
import nl.fontys.db3.backend.repository.LiveTelemetryRepository;
import nl.fontys.db3.backend.repository.TelemetryHistoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDeviceServiceTest {

    @Mock private DeviceRepository deviceRepository;
    @Mock private DeviceOwnershipRepository ownershipRepository;
    @Mock private LiveTelemetryRepository liveTelemetryRepository;
    @Mock private TelemetryHistoryRepository telemetryHistoryRepository;

    @InjectMocks
    private AdminDeviceService service;


    @Test
    void getDeviceById_success_returnsDTO() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(device.getDeviceId()).thenReturn("device1");
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.empty());
        when(liveTelemetryRepository.findByDeviceId("device1"))
                .thenReturn(Optional.empty());
        when(telemetryHistoryRepository.countByDeviceId("device1")).thenReturn(0L);
        when(telemetryHistoryRepository.findByDeviceIdOrderByTimestampDesc(eq("device1"), any()))
                .thenReturn(List.of());

        AdminDeviceDTO result = service.getDeviceById(1L);

        assertNotNull(result);
        verify(deviceRepository).findById(1L);
    }

    @Test
    void getDeviceByDeviceId_success_returnsDTO() {
        Device device = mock(Device.class);
        when(device.getDeviceId()).thenReturn("device1");
        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.of(device));
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.empty());
        when(liveTelemetryRepository.findByDeviceId("device1"))
                .thenReturn(Optional.empty());
        when(telemetryHistoryRepository.countByDeviceId("device1")).thenReturn(0L);
        when(telemetryHistoryRepository.findByDeviceIdOrderByTimestampDesc(eq("device1"), any()))
                .thenReturn(List.of());

        AdminDeviceDTO result = service.getDeviceByDeviceId("device1");

        assertNotNull(result);
        verify(deviceRepository).findByDeviceId("device1");
    }

    @Test
    void deactivateDevice_success() {
        Device device = mock(Device.class);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(device);

        service.deactivateDevice(1L);

        verify(device).setActive(false);
        verify(deviceRepository).save(device);
    }

    @Test
    void activateDevice_success() {
        Device device = mock(Device.class);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(device);

        service.activateDevice(1L);

        verify(device).setActive(true);
        verify(deviceRepository).save(device);
    }

    @ParameterizedTest
    @ValueSource(strings = {"new description", "test", ""})
    @NullAndEmptySource
    void updateDeviceDescription_success(String description) {
        Device device = mock(Device.class);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(device);

        service.updateDeviceDescription(1L, description);

        verify(device).setDescription(description);
        verify(deviceRepository).save(device);
    }

    @Test
    void toAdminDeviceDTO_includesOwnershipInfo() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(1L);
        when(device.getDeviceId()).thenReturn("device1");
        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.of(device));

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("testuser");

        DeviceOwnership ownership = mock(DeviceOwnership.class);
        when(ownership.getUser()).thenReturn(user);
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.of(ownership));
        when(liveTelemetryRepository.findByDeviceId("device1"))
                .thenReturn(Optional.empty());
        when(telemetryHistoryRepository.countByDeviceId("device1")).thenReturn(0L);
        when(telemetryHistoryRepository.findByDeviceIdOrderByTimestampDesc(eq("device1"), any()))
                .thenReturn(List.of());

        AdminDeviceDTO result = service.getDeviceByDeviceId("device1");

        assertNotNull(result);
        verify(ownershipRepository).findByDeviceIdAndActiveTrue("device1");
    }
}