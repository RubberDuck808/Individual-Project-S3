#include <Arduino.h>
#include <WiFi.h>
#include "secrets.h"

#ifdef WIFI_USERNAME
  // For WPA2-Enterprise support
  #include <esp_wpa2.h>
#endif

static const char* wlStatusToStr(wl_status_t s) {
  switch (s) {
    case WL_NO_SHIELD:        return "WL_NO_SHIELD";
    case WL_IDLE_STATUS:      return "WL_IDLE_STATUS";
    case WL_NO_SSID_AVAIL:    return "WL_NO_SSID_AVAIL (SSID not found)";
    case WL_SCAN_COMPLETED:   return "WL_SCAN_COMPLETED";
    case WL_CONNECTED:        return "WL_CONNECTED";
    case WL_CONNECT_FAILED:   return "WL_CONNECT_FAILED (wrong password?)";
    case WL_CONNECTION_LOST:  return "WL_CONNECTION_LOST";
    case WL_DISCONNECTED:     return "WL_DISCONNECTED";
    default:                  return "WL_UNKNOWN";
  }
}

static void printWifiInfo() {
  Serial.print("SSID: "); Serial.println(WiFi.SSID());
  Serial.print("RSSI: "); Serial.println(WiFi.RSSI());
  Serial.print("IP:   "); Serial.println(WiFi.localIP());
  Serial.print("GW:   "); Serial.println(WiFi.gatewayIP());
  Serial.print("Mask: "); Serial.println(WiFi.subnetMask());
  Serial.print("DNS:  "); Serial.println(WiFi.dnsIP());
  Serial.print("MAC:  "); Serial.println(WiFi.macAddress());
}

static void scanNetworks() {
  Serial.println("Scanning for WiFi networks...");
  int n = WiFi.scanNetworks(/*async=*/false, /*show_hidden=*/true);
  if (n < 0) {
    Serial.println("Scan failed.");
    return;
  }
  Serial.printf("Found %d networks:\n", n);
  for (int i = 0; i < n; i++) {
    Serial.printf("  %2d) %s  RSSI=%d  CH=%d  %s\n",
      i + 1,
      WiFi.SSID(i).c_str(),
      WiFi.RSSI(i),
      WiFi.channel(i),
      (WiFi.encryptionType(i) == WIFI_AUTH_OPEN) ? "OPEN" : "SECURED"
    );
  }
  Serial.println();
}

void initWiFi() {
  Serial.println("\n=== WiFi bring-up ===");

  Serial.print("Target SSID: ");
  Serial.println(WIFI_SSID);

  #ifdef WIFI_USERNAME
    Serial.print("Using enterprise WiFi with username: ");
    Serial.println(WIFI_USERNAME);
  #endif

  WiFi.mode(WIFI_STA);
  WiFi.setSleep(false);          // helps stability on some networks
  WiFi.disconnect(true, true);   // clear old creds + state
  delay(300);

  // See if hotspot is visible
  scanNetworks();

  Serial.println("Connecting...");
  
  #ifdef WIFI_USERNAME
    // Enterprise WiFi (WPA2-Enterprise) - requires username
    // Set WPA2 Enterprise authentication
    esp_wifi_sta_wpa2_ent_set_identity((uint8_t *)WIFI_USERNAME, strlen(WIFI_USERNAME));
    esp_wifi_sta_wpa2_ent_set_username((uint8_t *)WIFI_USERNAME, strlen(WIFI_USERNAME));
    esp_wifi_sta_wpa2_ent_set_password((uint8_t *)WIFI_PASS, strlen(WIFI_PASS));
    esp_wifi_sta_wpa2_ent_enable();
    
    // Now begin with just SSID (no password in begin() for enterprise)
    WiFi.begin(WIFI_SSID);
  #else
    // Standard WiFi (WPA2-PSK) - no username needed
    WiFi.begin(WIFI_SSID, WIFI_PASS);
  #endif

  unsigned long start = millis();
  wl_status_t last = WL_IDLE_STATUS;

  while (millis() - start < 20000) { // 20s timeout
    wl_status_t s = WiFi.status();
    if (s != last) {
      Serial.print("WiFi status changed: ");
      Serial.print(wlStatusToStr(s));
      Serial.print(" (");
      Serial.print((int)s);
      Serial.println(")");
      last = s;
    }
    if (s == WL_CONNECTED) break;
    delay(250);
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("WiFi connected!");
    printWifiInfo();
  } else {
    Serial.println("WiFi NOT connected within timeout.");
    Serial.print("Final status: ");
    Serial.println(wlStatusToStr(WiFi.status()));
  }
}
