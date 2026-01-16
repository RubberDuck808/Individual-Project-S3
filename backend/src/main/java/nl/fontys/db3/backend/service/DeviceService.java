package nl.fontys.db3.backend.service;

import lombok.extern.slf4j.Slf4j;
import nl.fontys.db3.backend.entity.Device;
import nl.fontys.db3.backend.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Register a new device and generate API key
     * Note: Device ownership is handled separately via DeviceOwnershipService
     * @param deviceId Unique device identifier
     * @param description Optional device description
     * @return Device with generated API key (only shown once!)
     */
    @Transactional
    public DeviceRegistrationResult registerDevice(String deviceId, String description) {
        log.debug("Registering device - deviceId: {}", deviceId);
        if (deviceId == null || deviceId.isBlank()) {
            log.warn("Device registration failed - deviceId is required");
            throw new IllegalArgumentException("deviceId is required");
        }

        Optional<Device> existing = deviceRepository.findByDeviceId(deviceId);
        if (existing.isPresent()) {
            log.warn("Device registration failed - device already registered: deviceId: {}", deviceId);
            throw new IllegalArgumentException("Device already registered");
        }

        String apiKey = generateApiKey();
        String apiKeyHash = hashApiKey(apiKey);

        Device device = Device.builder()
                .deviceId(deviceId)
                .apiKeyHash(apiKeyHash)
                .active(true)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        Device saved = deviceRepository.save(device);
        log.info("Device registered successfully - deviceId: {}, deviceDbId: {}", deviceId, saved.getId());

        return new DeviceRegistrationResult(saved, apiKey);
    }

    /**
     * Authenticate device using API key
     * @param apiKey The API key from X-API-Key header
     * @return Device if authenticated, empty otherwise
     */
    public Optional<Device> authenticateDevice(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("Device authentication failed - API key is null or blank");
            return Optional.empty();
        }

        String apiKeyHash = hashApiKey(apiKey);
        Optional<Device> device = deviceRepository.findByApiKeyHashAndActiveTrue(apiKeyHash);
        if (device.isPresent()) {
            log.debug("Device authenticated successfully - deviceId: {}", device.get().getDeviceId());
        } else {
            log.warn("Device authentication failed - invalid API key");
        }
        return device;
    }

    /**
     * Update last seen timestamp for device
     */
    @Transactional
    public void updateLastSeen(String deviceId) {
        log.debug("Updating last seen - deviceId: {}", deviceId);
        deviceRepository.findByDeviceId(deviceId)
                .ifPresent(device -> {
                    device.setLastSeenAt(LocalDateTime.now());
                    deviceRepository.save(device);
                });
    }

    /**
     * Generate a secure random API key
     */
    private String generateApiKey() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hash API key using SHA-256
     */
    private String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Result of device registration (contains API key shown only once)
     */
    public static class DeviceRegistrationResult {
        private final Device device;
        private final String apiKey; // Plaintext API key (show only once!)

        public DeviceRegistrationResult(Device device, String apiKey) {
            this.device = device;
            this.apiKey = apiKey;
        }

        public Device getDevice() {
            return device;
        }

        public String getApiKey() {
            return apiKey;
        }
    }
}
