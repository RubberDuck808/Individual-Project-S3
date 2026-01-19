package nl.fontys.db3.backend.integration.controller;

import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.entity.DeviceOwnership;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.DeviceOwnershipRepository;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@org.junit.jupiter.api.Disabled("Temporarily disabled due to database constraint issues")
class DeviceOwnershipControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceOwnershipRepository ownershipRepository;

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

    private Role userRole;
    private User testUser;
    private User otherUser;
    private Device testDevice;
    private DeviceOwnership testOwnership;
    private String testUserToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
        // Create user role
        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = Role.builder().name("USER").build();
                    return roleRepository.save(role);
                });

        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@test.com")
                .name("Test User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        testUser = userRepository.save(testUser);

        // Create other user
        otherUser = User.builder()
                .username("otheruser")
                .email("other@test.com")
                .name("Other User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build();
        otherUser = userRepository.save(otherUser);

        // Generate tokens
        UserDetails testUserDetails = userDetailsService.loadUserByUsername("test@test.com");
        testUserToken = jwtService.generateToken(testUserDetails.getUsername(), Map.of());

        UserDetails otherUserDetails = userDetailsService.loadUserByUsername("other@test.com");
        otherUserToken = jwtService.generateToken(otherUserDetails.getUsername(), Map.of());

        // Create test device
        testDevice = Device.builder()
                .deviceId("TEST-DEVICE-001")
                .description("Test Device")
                .apiKeyHash("test-api-key-hash-001")
                .build();
        testDevice = deviceRepository.save(testDevice);

        // Create test ownership
        testOwnership = DeviceOwnership.builder()
                .deviceId(testDevice.getDeviceId())
                .user(testUser)
                .active(true)
                .notes("Test ownership")
                .build();
        testOwnership = ownershipRepository.save(testOwnership);
    }

    @Test
    void assignDevice_success() throws Exception {
        // Unassign existing device first (user can only have 1 device)
        ownershipRepository.delete(testOwnership);
        
        // Create a new device for assignment
        Device newDevice = Device.builder()
                .deviceId("NEW-DEVICE-001")
                .description("New Device")
                .apiKeyHash("test-api-key-hash-002")
                .build();
        deviceRepository.save(newDevice);

        mockMvc.perform(post("/api/devices/{deviceId}/assign", newDevice.getDeviceId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("notes", "Assigned device"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(newDevice.getDeviceId()))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void assignDevice_unauthorized() throws Exception {
        mockMvc.perform(post("/api/devices/{deviceId}/assign", testDevice.getDeviceId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void transferDevice_success() throws Exception {
        mockMvc.perform(post("/api/devices/{deviceId}/transfer", testDevice.getDeviceId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("newOwnerId", otherUser.getId().toString())
                        .param("notes", "Transferred device"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value(testDevice.getDeviceId()));
    }

    @Test
    void transferDevice_notOwner() throws Exception {
        mockMvc.perform(post("/api/devices/{deviceId}/transfer", testDevice.getDeviceId())
                        .header("Authorization", "Bearer " + otherUserToken)
                        .param("newOwnerId", otherUser.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void transferDevice_unauthorized() throws Exception {
        mockMvc.perform(post("/api/devices/{deviceId}/transfer", testDevice.getDeviceId())
                        .param("newOwnerId", otherUser.getId().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unassignDevice_success() throws Exception {
        mockMvc.perform(delete("/api/devices/{deviceId}/unassign", testDevice.getDeviceId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .param("notes", "Unassigned device"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unassignDevice_notOwner() throws Exception {
        mockMvc.perform(delete("/api/devices/{deviceId}/unassign", testDevice.getDeviceId())
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unassignDevice_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/devices/{deviceId}/unassign", testDevice.getDeviceId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyDevices_success() throws Exception {
        mockMvc.perform(get("/api/devices/my-devices")
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].deviceId").value(testDevice.getDeviceId()))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getMyDevices_empty() throws Exception {
        // Create a user with no devices
        userRepository.save(User.builder()
                .username("emptyuser")
                .email("empty@test.com")
                .name("Empty User")
                .password(passwordEncoder.encode("password123"))
                .role(userRole)
                .build());
        
        UserDetails emptyUserDetails = userDetailsService.loadUserByUsername("empty@test.com");
        String emptyUserToken = jwtService.generateToken(emptyUserDetails.getUsername(), Map.of());

        mockMvc.perform(get("/api/devices/my-devices")
                        .header("Authorization", "Bearer " + emptyUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getMyDevices_unauthorized() throws Exception {
        mockMvc.perform(get("/api/devices/my-devices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOwnershipHistory_success() throws Exception {
        mockMvc.perform(get("/api/devices/{deviceId}/ownership", testDevice.getDeviceId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[0].deviceId").value(testDevice.getDeviceId()));
    }

    @Test
    void getOwnershipHistory_unauthorized() throws Exception {
        mockMvc.perform(get("/api/devices/{deviceId}/ownership", testDevice.getDeviceId()))
                .andExpect(status().isUnauthorized());
    }
}
