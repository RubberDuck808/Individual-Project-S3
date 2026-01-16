#ifndef SECRETS_H
#define SECRETS_H

// ============================================
// WiFi Configuration
// ============================================
#define WIFI_SSID "KPN1D1BA6"
#define WIFI_PASS "6WVMVcJL1L9tcHcM"

// // Iphone Hotspot
// #define WIFI_SSID "OscarIphone"
// #define WIFI_PASS "Rashid2005"

// Optional: For enterprise WiFi (WPA2-Enterprise), uncomment and set:
// #define WIFI_USERNAME "your-username"
// When WIFI_USERNAME is defined, WiFi.begin() will use enterprise mode

// School Wifi (Enterprise - requires username)
// #define WIFI_SSID "eduroam"
// #define WIFI_PASS "Zwubglespu5"
// #define WIFI_USERNAME "548789@student.fontys.nl"

// Backend Configuration
#define BACKEND_BASE_URL "http://192.168.2.2:8080"

// Device Configuration

// DEVICE_ID is now AUTO-GENERATED on first boot!
// The device will automatically generate: ESP32-{MAC_ADDRESS}-{RANDOM}

// DEVICE_API_KEY - Set this AFTER registering device in frontend
#define DEVICE_API_KEY "EAr-nZoKRm3zcyGesK9rvyiRQyinVPb08idqqwlhghY"  // Update after device registration

#endif