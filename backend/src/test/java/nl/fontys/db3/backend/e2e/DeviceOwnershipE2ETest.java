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

@DisplayName("E2E: Device Ownership Flow")
class DeviceOwnershipE2ETest extends BaseE2ETest {

    @BeforeEach
    void setUp() {
        setUpBase();
    }

    @Test
    @DisplayName("Register Device → View My Devices → Remove Device")
    void testDeviceOwnershipManagementFlow() throws Exception {
        // Register and login
        String registerRequest = objectMapper.writeValueAsString(Map.of(
                "username", "deviceowner",
                "email", "deviceowner@test.com",
                "password", "password123",
                "name", "Device Owner"
        ));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "deviceowner@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String authToken = "Bearer " + objectMapper.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<Map<String, Object>>() {}).get("token");

        // Register device
        String deviceRequest = objectMapper.writeValueAsString(Map.of(
                "deviceId", "ESP32-OWNERSHIP-TEST",
                "description", "Ownership Test Device"
        ));

        MvcResult deviceResult = mockMvc.perform(post("/api/devices/register")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deviceRequest))
                .andExpect(status().isOk())
                .andReturn();

        String deviceResponse = deviceResult.getResponse().getContentAsString();
        Map<String, Object> deviceData = objectMapper.readValue(deviceResponse, new TypeReference<Map<String, Object>>() {});
        String deviceId = deviceData.get("deviceIdentifier").toString();

        // View my devices
        mockMvc.perform(get("/api/devices/my-devices")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].deviceId").value(deviceId));

        // Remove device (unassign)
        mockMvc.perform(delete("/api/devices/{deviceId}/unassign", deviceId)
                        .header("Authorization", authToken))
                .andExpect(status().isNoContent());

        // Verify device removed
        mockMvc.perform(get("/api/devices/my-devices")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
