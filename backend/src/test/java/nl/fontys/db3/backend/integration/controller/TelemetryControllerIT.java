package nl.fontys.db3.backend.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.entity.LiveTelemetry;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.TelemetryHistory;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.DeviceRepository;
import nl.fontys.db3.backend.repository.LiveTelemetryRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.TelemetryHistoryRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TelemetryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private LiveTelemetryRepository liveTelemetryRepository;

    @Autowired
    private TelemetryHistoryRepository telemetryHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private nl.fontys.db3.backend.service.DeviceService deviceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Device testDevice;
    private String deviceId;
    private String apiKey;
    private String testUserToken;

    @BeforeEach
    void setUp() {
        deviceId = "test-device-123";
        // Register device to get a real API key
        nl.fontys.db3.backend.service.DeviceService.DeviceRegistrationResult result = 
            deviceService.registerDevice(deviceId, "Test Device");
        testDevice = result.getDevice();
        apiKey = result.getApiKey();

        // Create a test user for authenticated GET requests
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        User testUser = User.builder()
                .username("telemetryuser")
                .email("telemetry@test.com")
                .name("Telemetry User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername("telemetry@test.com");
        testUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());
    }

    @Test
    void upsertLive_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "deviceId", deviceId,
                "speedKph", 60.5,
                "rpm", 2500.0,
                "latitude", 51.4416,
                "longitude", 5.4697
        ));

        mockMvc.perform(put("/api/telemetry/live")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(deviceId))
                .andExpect(jsonPath("$.speedKph").value(60.5))
                .andExpect(jsonPath("$.rpm").value(2500.0));
    }

    @Test
    void getLive_success() throws Exception {
        // Create live telemetry
        LiveTelemetry live = LiveTelemetry.builder()
                .deviceId(deviceId)
                .speedKph(65.0)
                .rpm(3000.0)
                .latitude(51.4416)
                .longitude(5.4697)
                .lastUpdated(Instant.now())
                .build();
        liveTelemetryRepository.save(live);

        mockMvc.perform(get("/api/telemetry/live/" + deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(deviceId))
                .andExpect(jsonPath("$.speedKph").value(65.0));
    }

    @Test
    void getLive_notFound() throws Exception {
        mockMvc.perform(get("/api/telemetry/live/nonexistent-device"))
                .andExpect(status().isNotFound());
    }

    @Test
    void storeHistory_success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "deviceId", deviceId,
                "timestamp", Instant.now().toString(),
                "speedKph", 70.0,
                "rpm", 3500.0,
                "throttlePct", 50.0
        ));

        mockMvc.perform(post("/api/telemetry/history")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(deviceId))
                .andExpect(jsonPath("$.speedKph").value(70.0));
    }

    @Test
    void getHistory_success() throws Exception {
        // Create telemetry history
        TelemetryHistory history = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(Instant.now())
                .speedKph(75.0)
                .rpm(4000.0)
                .build();
        telemetryHistoryRepository.save(history);

        mockMvc.perform(get("/api/telemetry/history/" + deviceId)
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].deviceId").value(deviceId));
    }

    @Test
    void getHistory_withLimit() throws Exception {
        // Create multiple history entries
        for (int i = 0; i < 5; i++) {
            TelemetryHistory history = TelemetryHistory.builder()
                    .deviceId(deviceId)
                    .timestamp(Instant.now().plusSeconds(i))
                    .speedKph(70.0 + i)
                    .build();
            telemetryHistoryRepository.save(history);
        }

        mockMvc.perform(get("/api/telemetry/history/" + deviceId)
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getHistoryRange_success() throws Exception {
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();

        // Create history in range
        TelemetryHistory history = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(start.plusSeconds(1800))
                .speedKph(80.0)
                .build();
        telemetryHistoryRepository.save(history);

        mockMvc.perform(get("/api/telemetry/history/" + deviceId + "/range")
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getCarHealth_success() throws Exception {
        // Create telemetry history for health calculation
        TelemetryHistory history = TelemetryHistory.builder()
                .deviceId(deviceId)
                .timestamp(Instant.now())
                .speedKph(60.0)
                .rpm(2500.0)
                .coolantTempC(90.0)
                .batteryVoltageV(12.5)
                .build();
        telemetryHistoryRepository.save(history);

        mockMvc.perform(get("/api/telemetry/device/" + deviceId + "/health")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").exists());
    }

    @Test
    void getCarHealth_disconnected() throws Exception {
        mockMvc.perform(get("/api/telemetry/device/nonexistent-device/health")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    void getCarHealthHistory_success() throws Exception {
        // Create multiple history entries
        for (int i = 0; i < 3; i++) {
            TelemetryHistory history = TelemetryHistory.builder()
                    .deviceId(deviceId)
                    .timestamp(Instant.now().minusSeconds(i * 60))
                    .speedKph(70.0)
                    .rpm(3000.0)
                    .build();
            telemetryHistoryRepository.save(history);
        }

        mockMvc.perform(get("/api/telemetry/device/" + deviceId + "/health/history")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void upsertLive_invalidRequest() throws Exception {
        // Missing required deviceId
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "speedKph", 60.5
        ));

        mockMvc.perform(put("/api/telemetry/live")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isInternalServerError());
    }
}
