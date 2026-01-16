#pragma once
#include <Arduino.h>
#include <Preferences.h>
#include <WiFi.h>

/**
 * DeviceIdentity - Manages persistent device ID
 * 
 * Generates a unique device ID on first boot and stores it persistently.
 * The ID persists across reboots and only changes on factory reset.
 * 
 * ID Format: "ESP32-{MAC_ADDRESS}-{RANDOM_SUFFIX}"
 * Example: "ESP32-AABBCCDDEEFF-3A7B"
 */
class DeviceIdentity {
public:
  /**
   * Initialize device identity
   * - If ID exists in storage, loads it
   * - If not found, generates new unique ID and saves it
   * @return The device ID (either loaded or newly generated)
   */
  static String initialize();

  /**
   * Get the current device ID
   * Call initialize() first!
   * @return The device ID
   */
  static String getDeviceId();

  /**
   * Factory reset - clears stored device ID
   * Next call to initialize() will generate a new ID
   */
  static void factoryReset();

  /**
   * Check if device ID is already stored
   * @return true if ID exists in storage
   */
  static bool hasStoredId();

private:
  static String deviceId;
  static Preferences preferences;
  static const char* NAMESPACE;
  static const char* KEY_DEVICE_ID;
  static const char* KEY_INITIALIZED;

  /**
   * Generate a new unique device ID
   * Format: "ESP32-{MAC}-{RANDOM}"
   */
  static String generateDeviceId();

  /**
   * Load device ID from persistent storage
   * @return Device ID if found, empty string otherwise
   */
  static String loadDeviceId();

  /**
   * Save device ID to persistent storage
   * @param id The device ID to save
   */
  static void saveDeviceId(const String& id);

  /**
   * Generate random hex suffix
   * @param length Number of hex characters (default: 4)
   * @return Random hex string
   */
  static String generateRandomSuffix(int length = 4);
};
