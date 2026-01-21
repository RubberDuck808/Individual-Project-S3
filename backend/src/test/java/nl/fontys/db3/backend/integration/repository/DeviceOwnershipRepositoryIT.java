package nl.fontys.db3.backend.integration.repository;

import nl.fontys.db3.backend.entity.DeviceOwnership;
import nl.fontys.db3.backend.entity.Role;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.repository.DeviceOwnershipRepository;
import nl.fontys.db3.backend.repository.RoleRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest(excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration.class
})
class DeviceOwnershipRepositoryIT {

    @Autowired
    DeviceOwnershipRepository deviceOwnershipRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    private User user1;
    private User user2;
    private String deviceId = "ESP32-OWNERSHIP-TEST";

    @BeforeEach
    void setUp() {
        try {
            deviceOwnershipRepository.deleteAll();
            userRepository.deleteAll();
            // Don't delete roles - migration V4__Seed_roles.sql creates them
        } catch (Exception ignored) {
            // Tables may not exist yet
        }

        // Use existing USER role from migration
        Role role = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        user1 = User.builder()
                .username("owner1")
                .email("owner1@test.com")
                .name("Owner 1")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(user1);

        user2 = User.builder()
                .username("owner2")
                .email("owner2@test.com")
                .name("Owner 2")
                .password("encoded")
                .role(role)
                .build();
        userRepository.save(user2);
    }

    @Test
    void saveAndFindById() {
        DeviceOwnership ownership = DeviceOwnership.builder()
                .deviceId(deviceId)
                .user(user1)
                .active(true)
                .build();

        DeviceOwnership saved = deviceOwnershipRepository.save(ownership);
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        DeviceOwnership found = deviceOwnershipRepository.findById(saved.getId()).orElseThrow();
        assertEquals(deviceId, found.getDeviceId());
        assertEquals(user1.getId(), found.getUser().getId());
        assertTrue(found.isActive());
    }

    @Test
    void findByDeviceIdAndActiveTrue_Active() {
        DeviceOwnership ownership = DeviceOwnership.builder()
                .deviceId(deviceId)
                .user(user1)
                .active(true)
                .build();
        deviceOwnershipRepository.save(ownership);

        Optional<DeviceOwnership> found = deviceOwnershipRepository.findByDeviceIdAndActiveTrue(deviceId);
        assertTrue(found.isPresent());
        assertTrue(found.get().isActive());
    }

    @Test
    void findByDeviceIdAndActiveTrue_Inactive() {
        DeviceOwnership ownership = DeviceOwnership.builder()
                .deviceId(deviceId)
                .user(user1)
                .active(false)
                .build();
        deviceOwnershipRepository.save(ownership);

        Optional<DeviceOwnership> found = deviceOwnershipRepository.findByDeviceIdAndActiveTrue(deviceId);
        assertFalse(found.isPresent());
    }

    @Test
    void findByDeviceIdOrderByCreatedAtDesc() {
        LocalDateTime now = LocalDateTime.now();
        
        DeviceOwnership old = DeviceOwnership.builder()
                .deviceId(deviceId)
                .user(user1)
                .active(false)
                .createdAt(now.minusDays(1))
                .transferredAt(now)
                .build();
        deviceOwnershipRepository.save(old);

        DeviceOwnership current = DeviceOwnership.builder()
                .deviceId(deviceId)
                .user(user2)
                .active(true)
                .createdAt(now)
                .build();
        deviceOwnershipRepository.save(current);

        List<DeviceOwnership> history = deviceOwnershipRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
        assertEquals(2, history.size());
        assertTrue(history.get(0).getCreatedAt().isAfter(history.get(1).getCreatedAt()));
    }

    @Test
    void findByUser_IdAndActiveTrue() {
        DeviceOwnership ownership1 = DeviceOwnership.builder()
                .deviceId("DEVICE-1")
                .user(user1)
                .active(true)
                .build();
        deviceOwnershipRepository.save(ownership1);

        DeviceOwnership ownership2 = DeviceOwnership.builder()
                .deviceId("DEVICE-2")
                .user(user1)
                .active(true)
                .build();
        deviceOwnershipRepository.save(ownership2);

        DeviceOwnership inactive = DeviceOwnership.builder()
                .deviceId("DEVICE-3")
                .user(user1)
                .active(false)
                .build();
        deviceOwnershipRepository.save(inactive);

        List<DeviceOwnership> activeOwnerships = deviceOwnershipRepository.findByUser_IdAndActiveTrue(user1.getId());
        assertEquals(2, activeOwnerships.size());
    }

    @Test
    void findByUser_IdOrderByCreatedAtDesc() {
        LocalDateTime now = LocalDateTime.now();

        DeviceOwnership old = DeviceOwnership.builder()
                .deviceId("OLD-DEVICE")
                .user(user1)
                .active(false)
                .createdAt(now.minusDays(1))
                .transferredAt(now)
                .build();
        deviceOwnershipRepository.save(old);

        DeviceOwnership current = DeviceOwnership.builder()
                .deviceId("CURRENT-DEVICE")
                .user(user1)
                .active(true)
                .createdAt(now)
                .build();
        deviceOwnershipRepository.save(current);

        List<DeviceOwnership> history = deviceOwnershipRepository.findByUser_IdOrderByCreatedAtDesc(user1.getId());
        assertEquals(2, history.size());
        assertTrue(history.get(0).getCreatedAt().isAfter(history.get(1).getCreatedAt()));
    }

    @Test
    void transferOwnership() {
        LocalDateTime now = LocalDateTime.now();
        
        DeviceOwnership old = DeviceOwnership.builder()
                .deviceId(deviceId)
                .user(user1)
                .active(true)
                .createdAt(now.minusDays(1))
                .build();
        DeviceOwnership saved = deviceOwnershipRepository.save(old);
        deviceOwnershipRepository.flush(); // Ensure saved before deactivating

        saved.setActive(false);
        saved.setTransferredAt(now);
        saved.setNotes("Transferred to user2");
        deviceOwnershipRepository.saveAndFlush(saved); // Flush to ensure deactivation is persisted

        DeviceOwnership newOwnership = DeviceOwnership.builder()
                .deviceId(deviceId)
                .user(user2)
                .active(true)
                .createdAt(now)
                .build();
        deviceOwnershipRepository.save(newOwnership);

        Optional<DeviceOwnership> active = deviceOwnershipRepository.findByDeviceIdAndActiveTrue(deviceId);
        assertTrue(active.isPresent());
        assertEquals(user2.getId(), active.get().getUser().getId());
    }
}
