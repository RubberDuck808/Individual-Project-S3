package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.entity.DeviceOwnership;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.DeviceOwnershipRepository;
import nl.fontys.db3.backend.repository.DeviceRepository;
import nl.fontys.db3.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceOwnershipServiceTest {

    @Mock private DeviceOwnershipRepository ownershipRepository;
    @Mock private DeviceRepository deviceRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DeviceOwnershipService service;

    @Test
    void assignDeviceToUser_deviceNotFound_throws() {
        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.assignDeviceToUser("device1", 1L, "notes"));

        verify(deviceRepository).findByDeviceId("device1");
        verifyNoInteractions(userRepository, ownershipRepository);
    }

    @Test
    void assignDeviceToUser_userNotFound_throws() {
        Device device = mock(Device.class);
        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.of(device));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.assignDeviceToUser("device1", 1L, "notes"));

        verify(userRepository).findById(1L);
        verify(ownershipRepository, never()).save(any());
    }

    @Test
    void assignDeviceToUser_userHasDifferentDevice_throws() {
        Device device = mock(Device.class);
        User user = mock(User.class);
        DeviceOwnership existingOwnership = mock(DeviceOwnership.class);
        when(existingOwnership.getDeviceId()).thenReturn("device2");

        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.of(device));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ownershipRepository.findByUser_IdAndActiveTrue(1L))
                .thenReturn(List.of(existingOwnership));

        assertThrows(IllegalArgumentException.class,
                () -> service.assignDeviceToUser("device1", 1L, "notes"));

        verify(ownershipRepository, never()).save(any());
    }

    @Test
    void assignDeviceToUser_success_deactivatesExistingOwnership() {
        Device device = mock(Device.class);
        User user = mock(User.class);
        DeviceOwnership oldOwnership = mock(DeviceOwnership.class);

        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.of(device));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ownershipRepository.findByUser_IdAndActiveTrue(1L)).thenReturn(List.of());
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.of(oldOwnership));
        when(ownershipRepository.save(any(DeviceOwnership.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.assignDeviceToUser("device1", 1L, "notes");

        verify(oldOwnership).setActive(false);
        verify(oldOwnership).setTransferredAt(any(LocalDateTime.class));
        verify(ownershipRepository).save(oldOwnership);
    }

    @Test
    void assignDeviceToUser_success_createsNewOwnership() {
        Device device = mock(Device.class);
        User user = mock(User.class);

        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.of(device));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ownershipRepository.findByUser_IdAndActiveTrue(1L)).thenReturn(List.of());
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.empty());

        ArgumentCaptor<DeviceOwnership> captor = ArgumentCaptor.forClass(DeviceOwnership.class);
        when(ownershipRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        DeviceOwnership result = service.assignDeviceToUser("device1", 1L, "test notes");

        assertNotNull(result);
        DeviceOwnership saved = captor.getValue();
        assertEquals("device1", saved.getDeviceId());
        assertSame(user, saved.getUser());
        assertTrue(saved.isActive());
        assertEquals("test notes", saved.getNotes());
        assertNotNull(saved.getCreatedAt());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void unassignDevice_noActiveOwnership_noOp(String deviceId) {
        when(ownershipRepository.findByDeviceIdAndActiveTrue(deviceId))
                .thenReturn(Optional.empty());

        service.unassignDevice(deviceId, "notes");

        verify(ownershipRepository, never()).save(any());
    }

    @Test
    void unassignDevice_success_deactivatesOwnership() {
        DeviceOwnership ownership = mock(DeviceOwnership.class);
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.of(ownership));
        when(ownershipRepository.save(ownership)).thenReturn(ownership);

        service.unassignDevice("device1", "unassign notes");

        verify(ownership).setActive(false);
        verify(ownership).setTransferredAt(any(LocalDateTime.class));
        verify(ownership).setNotes("unassign notes");
        verify(ownershipRepository).save(ownership);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void getCurrentOwner_noOwnership_returnsEmpty(String deviceId) {
        when(ownershipRepository.findByDeviceIdAndActiveTrue(deviceId))
                .thenReturn(Optional.empty());

        Optional<User> result = service.getCurrentOwner(deviceId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentOwner_success_returnsUser() {
        User user = mock(User.class);
        DeviceOwnership ownership = mock(DeviceOwnership.class);
        when(ownership.getUser()).thenReturn(user);
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.of(ownership));

        Optional<User> result = service.getCurrentOwner("device1");

        assertTrue(result.isPresent());
        assertSame(user, result.get());
    }

    @Test
    void isOwner_sameUser_returnsTrue() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        DeviceOwnership ownership = mock(DeviceOwnership.class);
        when(ownership.getUser()).thenReturn(user);
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.of(ownership));

        boolean result = service.isOwner("device1", 1L);

        assertTrue(result);
    }

    @Test
    void isOwner_differentUser_returnsFalse() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(2L);
        DeviceOwnership ownership = mock(DeviceOwnership.class);
        when(ownership.getUser()).thenReturn(user);
        when(ownershipRepository.findByDeviceIdAndActiveTrue("device1"))
                .thenReturn(Optional.of(ownership));

        boolean result = service.isOwner("device1", 1L);

        assertFalse(result);
    }
}