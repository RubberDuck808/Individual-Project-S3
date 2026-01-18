package nl.fontys.db3.backend.service;

import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.repository.DeviceRepository;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService service;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void registerDevice_invalidDeviceId_throws(String deviceId) {
        assertThrows(IllegalArgumentException.class,
                () -> service.registerDevice(deviceId, "description"));
        verifyNoInteractions(deviceRepository);
    }

    @Test
    void registerDevice_alreadyExists_throws() {
        Device existing = mock(Device.class);
        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> service.registerDevice("device1", "description"));

        verify(deviceRepository).findByDeviceId("device1");
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void registerDevice_success_createsDeviceWithApiKey() {
        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.empty());
        when(deviceRepository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceService.DeviceRegistrationResult result = service.registerDevice("device1", "test device");

        assertNotNull(result);
        assertNotNull(result.getDevice());
        assertNotNull(result.getApiKey());
        assertFalse(result.getApiKey().isEmpty());

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceRepository).save(captor.capture());

        Device saved = captor.getValue();
        assertEquals("device1", saved.getDeviceId());
        assertEquals("test device", saved.getDescription());
        assertTrue(saved.isActive());
        assertNotNull(saved.getApiKeyHash());
        assertNotNull(saved.getCreatedAt());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void authenticateDevice_invalidApiKey_returnsEmpty(String apiKey) {
        Optional<Device> result = service.authenticateDevice(apiKey);

        assertTrue(result.isEmpty());
        verifyNoInteractions(deviceRepository);
    }

    @Test
    void authenticateDevice_validApiKey_returnsDevice() {
        Device device = mock(Device.class);
        String apiKey = "test-api-key";
        when(deviceRepository.findByApiKeyHashAndActiveTrue(anyString()))
                .thenReturn(Optional.of(device));

        Optional<Device> result = service.authenticateDevice(apiKey);

        assertTrue(result.isPresent());
        assertSame(device, result.get());
        verify(deviceRepository).findByApiKeyHashAndActiveTrue(anyString());
    }

    @Test
    void updateLastSeen_success_updatesTimestamp() {
        Device device = mock(Device.class);
        when(deviceRepository.findByDeviceId("device1")).thenReturn(Optional.of(device));
        when(deviceRepository.save(device)).thenReturn(device);

        service.updateLastSeen("device1");

        verify(device).setLastSeenAt(any(LocalDateTime.class));
        verify(deviceRepository).save(device);
    }
}