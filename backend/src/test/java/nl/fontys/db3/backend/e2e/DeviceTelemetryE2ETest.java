package nl.fontys.db3.backend.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("E2E: Device Telemetry Flow")
class DeviceTelemetryE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        setUpBase();
    }

    @Test
    @DisplayName("User Registration → Login → Register Device → Send Telemetry → View Car Health")
    void testCompleteDeviceTelemetryFlow() throws Exception {
        // Step 1: Register and login
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "devicetest",
                "email", "devicetest@test.com",
                "password", "password123",
                "name", "Device Test User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "devicetest@test.com",
                "password", "password123"
        ));

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        Map<String, Object> loginData = objectMapper.readValue(loginResponse, new TypeReference<Map<String, Object>>() {});
        String authToken = "Bearer " + loginData.get("token");

        // Step 2: Register a device
        String deviceRequest = objectMapper.writeValueAsString(Map.of(
                "deviceId", "ESP32-E2E-TEST-001",
                "description", "E2E Test Device",
                "deviceType", "ESP32",
                "firmwareVersion", "1.0.0"
        ));

        MvcResult deviceResult = mockMvc.perform(post("/api/devices/register")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deviceRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceIdentifier").value("ESP32-E2E-TEST-001"))
                .andExpect(jsonPath("$.apiKey").exists())
                .andReturn();

        String deviceResponse = deviceResult.getResponse().getContentAsString();
        Map<String, Object> deviceData = objectMapper.readValue(deviceResponse, new TypeReference<Map<String, Object>>() {});
        String apiKey = deviceData.get("apiKey").toString();

        // Step 3: Send telemetry data to history using device API key
        String telemetryRequest = objectMapper.writeValueAsString(Map.of(
                "deviceId", "ESP32-E2E-TEST-001",
                "speedKph", 60.5,
                "rpm", 2500.0,
                "throttlePct", 45.0,
                "coolantTempC", 85.0,
                "batteryVoltageV", 12.6
        ));

        mockMvc.perform(post("/api/telemetry/history")
                        .header("X-API-Key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(telemetryRequest))
                .andExpect(status().isOk());

        // Step 4: View car health (requires deviceId in path)
        mockMvc.perform(get("/api/telemetry/device/{deviceId}/health", "ESP32-E2E-TEST-001")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speedKmh").value(60.5))
                .andExpect(jsonPath("$.rpm").value(2500.0))
                .andExpect(jsonPath("$.coolantC").value(85.0));
    }

    @Test
    @DisplayName("Register Device → Send Multiple Telemetry Updates → View Telemetry History")
    void testTelemetryHistoryFlow() throws Exception {
        // Register and login
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "telemetryuser",
                "email", "telemetryuser@test.com",
                "password", "password123",
                "name", "Telemetry User"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        String loginRequest = objectMapper.writeValueAsString(Map.of(
                "email", "telemetryuser@test.com",
                "password", "password123"
        ));

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        Map<String, Object> loginData = objectMapper.readValue(loginResponse, new TypeReference<Map<String, Object>>() {});
        String authToken = "Bearer " + loginData.get("token");

        // Register device
        String deviceRequest = objectMapper.writeValueAsString(Map.of(
                "deviceId", "ESP32-HISTORY-TEST",
                "description", "History Test Device"
        ));

        MvcResult deviceResult = mockMvc.perform(post("/api/devices/register")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deviceRequest))
                .andExpect(status().isOk())
                .andReturn();

        String deviceResponse = deviceResult.getResponse().getContentAsString();
        Map<String, Object> deviceData = objectMapper.readValue(deviceResponse, new TypeReference<Map<String, Object>>() {});
        String apiKey = deviceData.get("apiKey").toString();

        // Send multiple telemetry updates to history
        for (int i = 0; i < 3; i++) {
            String telemetryRequest = objectMapper.writeValueAsString(Map.of(
                    "deviceId", "ESP32-HISTORY-TEST",
                    "speedKph", 50.0 + (i * 10),
                    "rpm", 2000.0 + (i * 100),
                    "throttlePct", 30.0 + (i * 5)
            ));

            mockMvc.perform(post("/api/telemetry/history")
                            .header("X-API-Key", apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(telemetryRequest))
                    .andExpect(status().isOk());
        }

        // View telemetry history (deviceId in path)
        mockMvc.perform(get("/api/telemetry/history/{deviceId}", "ESP32-HISTORY-TEST")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
