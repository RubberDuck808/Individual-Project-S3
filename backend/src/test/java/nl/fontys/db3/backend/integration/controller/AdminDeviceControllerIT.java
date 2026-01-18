package nl.fontys.db3.backend.integration.controller;

import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.DeviceRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import nl.fontys.db3.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminDeviceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Role adminRole;
    private Role userRole;
    private User adminUser;
    private User regularUser;
    private Device testDevice;
    private String adminToken;
    private String regularUserToken;

    @BeforeEach
    void setUp() {
        // Create ADMIN role
        adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = Role.builder().name("ADMIN").build();
                    return roleRepository.save(role);
                });

        // Create USER role
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        // Create admin user
        adminUser = User.builder()
                .username("admin")
                .email("admin@test.com")
                .name("Admin User")
                .password(passwordEncoder.encode("password123"))
                .role(adminRole)
                .build();
        adminUser = userRepository.save(adminUser);

        // Create regular user
        regularUser = User.builder()
                .username("regularuser")
                .email("regular@test.com")
                .name("Regular User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        regularUser = userRepository.save(regularUser);

        // Create test device
        String apiKeyHash = Base64.getEncoder().encodeToString("test-api-key".getBytes());
        testDevice = Device.builder()
                .deviceId("test-device-123")
                .apiKeyHash(apiKeyHash)
                .active(true)
                .description("Test Device")
                .createdAt(LocalDateTime.now())
                .build();
        testDevice = deviceRepository.save(testDevice);

        // Generate tokens
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin@test.com");
        adminToken = jwtService.generateToken(adminDetails.getUsername(), Map.of());

        UserDetails userDetails = userDetailsService.loadUserByUsername("regular@test.com");
        regularUserToken = jwtService.generateToken(userDetails.getUsername(), Map.of());
    }

    @Test
    void getAllDevices_success() throws Exception {
        mockMvc.perform(get("/api/admin/devices")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllDevices_withPagination() throws Exception {
        mockMvc.perform(get("/api/admin/devices")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAllDevices_unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/devices")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDeviceById_success() throws Exception {
        mockMvc.perform(get("/api/admin/devices/" + testDevice.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testDevice.getId()));
    }

    @Test
    void getDeviceById_notFound() throws Exception {
        mockMvc.perform(get("/api/admin/devices/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDeviceByDeviceId_success() throws Exception {
        mockMvc.perform(get("/api/admin/devices/device-id/" + testDevice.getDeviceId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(testDevice.getDeviceId()));
    }

    @Test
    void getDeviceByDeviceId_notFound() throws Exception {
        mockMvc.perform(get("/api/admin/devices/device-id/nonexistent-device")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateDevice_success() throws Exception {
        // First deactivate the device
        testDevice.setActive(false);
        deviceRepository.save(testDevice);

        mockMvc.perform(put("/api/admin/devices/" + testDevice.getId() + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify device is active
        Device updated = deviceRepository.findById(testDevice.getId()).orElseThrow();
        assertTrue(updated.isActive());
    }

    @Test
    void deactivateDevice_success() throws Exception {
        mockMvc.perform(put("/api/admin/devices/" + testDevice.getId() + "/deactivate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify device is inactive
        Device updated = deviceRepository.findById(testDevice.getId()).orElseThrow();
        assertFalse(updated.isActive());
    }

    @Test
    void updateDescription_success() throws Exception {
        String newDescription = "Updated Description";
        mockMvc.perform(put("/api/admin/devices/" + testDevice.getId() + "/description")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("description", newDescription))
                .andExpect(status().isOk());

        // Verify description is updated
        Device updated = deviceRepository.findById(testDevice.getId()).orElseThrow();
        assertEquals(newDescription, updated.getDescription());
    }
}
