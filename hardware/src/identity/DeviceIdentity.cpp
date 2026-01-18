#include "DeviceIdentity.h"
#include <esp_system.h>

// Static member initialization
String DeviceIdentity::deviceId = "";
Preferences DeviceIdentity::preferences;
const char* DeviceIdentity::NAMESPACE = "device";
const char* DeviceIdentity::KEY_DEVICE_ID = "id";
const char* DeviceIdentity::KEY_INITIALIZED = "init";

String DeviceIdentity::initialize() {
  // Open preferences namespace
  preferences.begin(NAMESPACE, false);

  // Try to load existing ID
  deviceId = loadDeviceId();

  if (deviceId.length() == 0) {
    // No stored ID found - generate new one
    Serial.println("[DeviceIdentity] No stored device ID found. Generating new ID...");
    deviceId = generateDeviceId();
    saveDeviceId(deviceId);
    Serial.printf("[DeviceIdentity] Generated new device ID: %s\n", deviceId.c_str());
  } else {
    Serial.printf("[DeviceIdentity] Loaded existing device ID: %s\n", deviceId.c_str());
  }

  preferences.end();
  return deviceId;
}

String DeviceIdentity::getDeviceId() {
  if (deviceId.length() == 0) {
    Serial.println("[DeviceIdentity] WARNING: Device ID not initialized. Call initialize() first!");
    return initialize(); // Auto-initialize if not done
  }
  return deviceId;
}

void DeviceIdentity::factoryReset() {
  preferences.begin(NAMESPACE, false);
  preferences.remove(KEY_DEVICE_ID);
  preferences.remove(KEY_INITIALIZED);
  preferences.end();
  deviceId = "";
  Serial.println("[DeviceIdentity] Factory reset complete. Device ID cleared.");
}

bool DeviceIdentity::hasStoredId() {
  preferences.begin(NAMESPACE, true); // Read-only
  bool exists = preferences.isKey(KEY_DEVICE_ID);
  preferences.end();
  return exists;
}

String DeviceIdentity::generateDeviceId() {
  // Get MAC address
  uint8_t mac[6];
  esp_read_mac(mac, ESP_MAC_WIFI_STA);
  
  char macStr[13];
  sprintf(macStr, "%02X%02X%02X%02X%02X%02X", 
          mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
  
  String randomSuffix = generateRandomSuffix(4);
  
  String id = "ESP32-";
  id += macStr;
  id += "-";
  id += randomSuffix;
  
  return id;
}

String DeviceIdentity::loadDeviceId() {
  if (!preferences.isKey(KEY_DEVICE_ID)) {
    return "";
  }
  
  String id = preferences.getString(KEY_DEVICE_ID, "");
  
  if (id.length() < 10 || !id.startsWith("ESP32-")) {
    Serial.println("[DeviceIdentity] WARNING: Stored ID format invalid. Will regenerate.");
    return "";
  }
  
  return id;
}

void DeviceIdentity::saveDeviceId(const String& id) {
  preferences.putString(KEY_DEVICE_ID, id);
  preferences.putBool(KEY_INITIALIZED, true);
  Serial.printf("[DeviceIdentity] Saved device ID to persistent storage\n");
}

String DeviceIdentity::generateRandomSuffix(int length) {
  String suffix = "";
  const char hexChars[] = "0123456789ABCDEF";
  
  for (int i = 0; i < length; i++) {
    uint32_t randomValue = esp_random();
    suffix += hexChars[randomValue % 16];
  }
  
  return suffix;
}
