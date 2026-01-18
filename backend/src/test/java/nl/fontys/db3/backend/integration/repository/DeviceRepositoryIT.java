package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class DeviceRepositoryIT {

    @Autowired
    DeviceRepository deviceRepository;

    @BeforeEach
    void setUp() {
        try {
            deviceRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }
    }

    @Test
    void saveAndFindById() {
        Device device = Device.builder()
                .deviceId("ESP32-TEST-001")
                .apiKeyHash("hashed-key-123")
                .active(true)
                .description("Test Device")
                .deviceType("ESP32")
                .firmwareVersion("1.0.0")
                .build();

        Device saved = deviceRepository.save(device);
        assertNotNull(saved.getId());

        Optional<Device> found = deviceRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("ESP32-TEST-001", found.get().getDeviceId());
        assertEquals("hashed-key-123", found.get().getApiKeyHash());
    }

    @Test
    void findByDeviceId() {
        Device device = Device.builder()
                .deviceId("ESP32-FIND-001")
                .apiKeyHash("hash-001")
                .active(true)
                .build();
        deviceRepository.save(device);

        Optional<Device> found = deviceRepository.findByDeviceId("ESP32-FIND-001");
        assertTrue(found.isPresent());
        assertEquals("ESP32-FIND-001", found.get().getDeviceId());
    }

    @Test
    void findByDeviceId_NotFound() {
        Optional<Device> found = deviceRepository.findByDeviceId("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void findByApiKeyHash() {
        Device device = Device.builder()
                .deviceId("ESP32-KEY-001")
                .apiKeyHash("unique-hash-123")
                .active(true)
                .build();
        deviceRepository.save(device);

        Optional<Device> found = deviceRepository.findByApiKeyHash("unique-hash-123");
        assertTrue(found.isPresent());
        assertEquals("unique-hash-123", found.get().getApiKeyHash());
    }

    @Test
    void findByDeviceIdAndActiveTrue_ActiveDevice() {
        Device device = Device.builder()
                .deviceId("ESP32-ACTIVE-001")
                .apiKeyHash("hash-active")
                .active(true)
                .build();
        deviceRepository.save(device);

        Optional<Device> found = deviceRepository.findByDeviceIdAndActiveTrue("ESP32-ACTIVE-001");
        assertTrue(found.isPresent());
        assertTrue(found.get().isActive());
    }

    @Test
    void findByDeviceIdAndActiveTrue_InactiveDevice() {
        Device device = Device.builder()
                .deviceId("ESP32-INACTIVE-001")
                .apiKeyHash("hash-inactive")
                .active(false)
                .build();
        deviceRepository.save(device);

        Optional<Device> found = deviceRepository.findByDeviceIdAndActiveTrue("ESP32-INACTIVE-001");
        assertFalse(found.isPresent());
    }

    @Test
    void findByApiKeyHashAndActiveTrue_ActiveDevice() {
        Device device = Device.builder()
                .deviceId("ESP32-ACTIVE-KEY")
                .apiKeyHash("active-key-hash")
                .active(true)
                .build();
        deviceRepository.save(device);

        Optional<Device> found = deviceRepository.findByApiKeyHashAndActiveTrue("active-key-hash");
        assertTrue(found.isPresent());
        assertTrue(found.get().isActive());
    }

    @Test
    void findByApiKeyHashAndActiveTrue_InactiveDevice() {
        Device device = Device.builder()
                .deviceId("ESP32-INACTIVE-KEY")
                .apiKeyHash("inactive-key-hash")
                .active(false)
                .build();
        deviceRepository.save(device);

        Optional<Device> found = deviceRepository.findByApiKeyHashAndActiveTrue("inactive-key-hash");
        assertFalse(found.isPresent());
    }

    @Test
    void updateLastSeenAt() {
        Device device = Device.builder()
                .deviceId("ESP32-UPDATE-001")
                .apiKeyHash("hash-update")
                .active(true)
                .build();
        Device saved = deviceRepository.save(device);

        LocalDateTime now = LocalDateTime.now();
        saved.setLastSeenAt(now);
        Device updated = deviceRepository.save(saved);

        assertEquals(now, updated.getLastSeenAt());
    }

    @Test
    void deactivateDevice() {
        Device device = Device.builder()
                .deviceId("ESP32-DEACTIVATE")
                .apiKeyHash("hash-deactivate")
                .active(true)
                .build();
        Device saved = deviceRepository.save(device);

        saved.setActive(false);
        Device updated = deviceRepository.save(saved);

        assertFalse(updated.isActive());
        Optional<Device> found = deviceRepository.findByDeviceIdAndActiveTrue("ESP32-DEACTIVATE");
        assertFalse(found.isPresent());
    }

    @Test
    void uniqueConstraint_DeviceId() {
        Device device1 = Device.builder()
                .deviceId("ESP32-UNIQUE")
                .apiKeyHash("hash1")
                .active(true)
                .build();
        deviceRepository.save(device1);

        Device device2 = Device.builder()
                .deviceId("ESP32-UNIQUE")
                .apiKeyHash("hash2")
                .active(true)
                .build();

        assertThrows(Exception.class, () -> deviceRepository.saveAndFlush(device2));
    }

    @Test
    void uniqueConstraint_ApiKeyHash() {
        Device device1 = Device.builder()
                .deviceId("ESP32-1")
                .apiKeyHash("same-hash")
                .active(true)
                .build();
        deviceRepository.save(device1);

        Device device2 = Device.builder()
                .deviceId("ESP32-2")
                .apiKeyHash("same-hash")
                .active(true)
                .build();

        assertThrows(Exception.class, () -> deviceRepository.saveAndFlush(device2));
    }
}
