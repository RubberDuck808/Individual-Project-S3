package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.LiveTelemetry;
import nl.fontys.db3.backend.repository.LiveTelemetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class LiveTelemetryRepositoryIT {

    @Autowired
    LiveTelemetryRepository liveTelemetryRepository;

    private String deviceId = "ESP32-LIVE-TEST";

    @BeforeEach
    void setUp() {
        try {
            liveTelemetryRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }
    }

    @Test
    void saveAndFindById() {
        Instant now = Instant.now();
        LiveTelemetry telemetry = LiveTelemetry.builder()
                .deviceId(deviceId)
                .lastUpdated(now)
                .speedKph(60.5)
                .rpm(2500.0)
                .latitude(51.4416)
                .longitude(5.4697)
                .build();

        LiveTelemetry saved = liveTelemetryRepository.save(telemetry);
        assertNotNull(saved.getId());

        LiveTelemetry found = liveTelemetryRepository.findById(saved.getId()).orElseThrow();
        assertEquals(deviceId, found.getDeviceId());
        assertEquals(60.5, found.getSpeedKph());
        assertEquals(2500.0, found.getRpm());
        assertEquals(51.4416, found.getLatitude());
        assertEquals(5.4697, found.getLongitude());
    }

    @Test
    void findByDeviceId() {
        Instant now = Instant.now();
        LiveTelemetry telemetry = LiveTelemetry.builder()
                .deviceId(deviceId)
                .lastUpdated(now)
                .speedKph(55.0)
                .rpm(2200.0)
                .build();
        liveTelemetryRepository.save(telemetry);

        Optional<LiveTelemetry> found = liveTelemetryRepository.findByDeviceId(deviceId);
        assertTrue(found.isPresent());
        assertEquals(deviceId, found.get().getDeviceId());
        assertEquals(55.0, found.get().getSpeedKph());
    }

    @Test
    void findByDeviceId_NotFound() {
        Optional<LiveTelemetry> found = liveTelemetryRepository.findByDeviceId("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void updateExistingRecord() {
        Instant now = Instant.now();
        LiveTelemetry telemetry = LiveTelemetry.builder()
                .deviceId(deviceId)
                .lastUpdated(now)
                .speedKph(50.0)
                .rpm(2000.0)
                .build();
        LiveTelemetry saved = liveTelemetryRepository.save(telemetry);

        saved.setSpeedKph(70.0);
        saved.setRpm(3000.0);
        saved.setLastUpdated(Instant.now());
        LiveTelemetry updated = liveTelemetryRepository.save(saved);

        assertEquals(70.0, updated.getSpeedKph());
        assertEquals(3000.0, updated.getRpm());
    }

    @Test
    void uniqueConstraint_DeviceId() {
        Instant now = Instant.now();
        LiveTelemetry telemetry1 = LiveTelemetry.builder()
                .deviceId(deviceId)
                .lastUpdated(now)
                .speedKph(50.0)
                .build();
        liveTelemetryRepository.save(telemetry1);

        LiveTelemetry telemetry2 = LiveTelemetry.builder()
                .deviceId(deviceId)
                .lastUpdated(Instant.now())
                .speedKph(60.0)
                .build();

        assertThrows(Exception.class, () -> liveTelemetryRepository.saveAndFlush(telemetry2));
    }

    @Test
    void deleteOlderThan() {
        Instant cutoff = Instant.now().minusSeconds(3600); // 1 hour
        Instant old = cutoff.minusSeconds(3600); // 1 hour before
        Instant recent = cutoff.plusSeconds(3600); // 1 hour after

        LiveTelemetry oldTelemetry = LiveTelemetry.builder()
                .deviceId("OLD-DEVICE")
                .lastUpdated(old)
                .speedKph(50.0)
                .build();
        liveTelemetryRepository.save(oldTelemetry);

        LiveTelemetry recentTelemetry = LiveTelemetry.builder()
                .deviceId("RECENT-DEVICE")
                .lastUpdated(recent)
                .speedKph(60.0)
                .build();
        liveTelemetryRepository.save(recentTelemetry);

        liveTelemetryRepository.deleteOlderThan(cutoff);

        Optional<LiveTelemetry> remaining = liveTelemetryRepository.findByDeviceId("RECENT-DEVICE");
        assertTrue(remaining.isPresent());

        Optional<LiveTelemetry> deleted = liveTelemetryRepository.findByDeviceId("OLD-DEVICE");
        assertFalse(deleted.isPresent());
    }
}
