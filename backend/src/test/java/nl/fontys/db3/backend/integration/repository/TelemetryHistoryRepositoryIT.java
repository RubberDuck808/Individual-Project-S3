package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.TelemetryHistory;
import nl.fontys.db3.backend.repository.TelemetryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb",
    "spring.datasource.username=test",
    "spring.datasource.password=test",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=true"
})
class TelemetryHistoryRepositoryIT {

    @Autowired
    TelemetryHistoryRepository telemetryHistoryRepository;

    private String deviceId = "ESP32-HISTORY-TEST";

    @BeforeEach
    void setUp() {
        try {
            telemetryHistoryRepository.deleteAll();
        } catch (Exception ignored) {
            // Tables may not exist yet
        }
    }

    @Test
    void saveAndFindById() {
        Instant now = Instant.now();
        TelemetryHistory telemetry = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(now)
                .speedKph(60.5)
                .rpm(2500.0)
                .throttlePct(45.0)
                .coolantTempC(85.0)
                .batteryVoltageV(12.6)
                .build();

        TelemetryHistory saved = telemetryHistoryRepository.save(telemetry);
        assertNotNull(saved.getId());

        TelemetryHistory found = telemetryHistoryRepository.findById(saved.getId()).orElseThrow();
        assertEquals(deviceId, found.getDeviceId());
        assertEquals(60.5, found.getSpeedKph());
        assertEquals(2500.0, found.getRpm());
    }

    @Test
    void findByDeviceIdOrderByTimestampDesc() {
        Instant now = Instant.now();
        
        TelemetryHistory old = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(now.minusSeconds(300))
                .speedKph(50.0)
                .rpm(2000.0)
                .build();
        telemetryHistoryRepository.save(old);

        TelemetryHistory recent = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(now)
                .speedKph(60.0)
                .rpm(2500.0)
                .build();
        telemetryHistoryRepository.save(recent);

        Pageable pageable = PageRequest.of(0, 10);
        List<TelemetryHistory> history = telemetryHistoryRepository.findByDeviceIdOrderByTimestampDesc(deviceId, pageable);
        
        assertEquals(2, history.size());
        assertTrue(history.get(0).getTimestamp().isAfter(history.get(1).getTimestamp()));
    }

    @Test
    void findByDeviceIdAndTimestampBetweenOrderByTimestampDesc() {
        Instant start = Instant.now().minusSeconds(600);
        Instant end = Instant.now();
        Instant before = start.minusSeconds(100);
        Instant after = end.plusSeconds(100);

        TelemetryHistory beforeRange = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(before)
                .speedKph(40.0)
                .build();
        telemetryHistoryRepository.save(beforeRange);

        TelemetryHistory inRange1 = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(start.plusSeconds(100))
                .speedKph(50.0)
                .build();
        telemetryHistoryRepository.save(inRange1);

        TelemetryHistory inRange2 = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(end.minusSeconds(50))
                .speedKph(60.0)
                .build();
        telemetryHistoryRepository.save(inRange2);

        TelemetryHistory afterRange = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(after)
                .speedKph(70.0)
                .build();
        telemetryHistoryRepository.save(afterRange);

        Pageable pageable = PageRequest.of(0, 10);
        List<TelemetryHistory> inRange = telemetryHistoryRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampDesc(
                deviceId, start, end, pageable
        );

        assertEquals(2, inRange.size());
    }

    @Test
    void deleteByDeviceIdAndTimestampBefore() {
        Instant cutoff = Instant.now().minusSeconds(86400); // 24 hours
        Instant old = cutoff.minusSeconds(3600); // 1 hour before
        Instant recent = cutoff.plusSeconds(3600); // 1 hour after

        TelemetryHistory oldRecord = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(old)
                .speedKph(50.0)
                .build();
        telemetryHistoryRepository.save(oldRecord);

        TelemetryHistory recentRecord = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(recent)
                .speedKph(60.0)
                .build();
        telemetryHistoryRepository.save(recentRecord);

        telemetryHistoryRepository.deleteByDeviceIdAndTimestampBefore(deviceId, cutoff);

        List<TelemetryHistory> remaining = telemetryHistoryRepository.findAll();
        assertEquals(1, remaining.size());
        assertEquals(recentRecord.getId(), remaining.get(0).getId());
    }

    @Test
    void countByDeviceId() {
        TelemetryHistory record1 = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(Instant.now())
                .speedKph(50.0)
                .build();
        telemetryHistoryRepository.save(record1);

        TelemetryHistory record2 = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(Instant.now().plusSeconds(60))
                .speedKph(60.0)
                .build();
        telemetryHistoryRepository.save(record2);

        TelemetryHistory otherDevice = TelemetryHistory.builder()
                .deviceId("OTHER-DEVICE")
                .timestamp(Instant.now())
                .speedKph(40.0)
                .build();
        telemetryHistoryRepository.save(otherDevice);

        long count = telemetryHistoryRepository.countByDeviceId(deviceId);
        assertEquals(2, count);
    }
}
