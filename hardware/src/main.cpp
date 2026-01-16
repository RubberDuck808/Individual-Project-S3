#include <Arduino.h>
#include "secrets.h"
#include "./network/network.h"

// Configuration
#include "config/Config.h"

// Device identity (persistent device ID)
#include "identity/DeviceIdentity.h"

// Data source (switched via Config.h)
#include "data/DataSourceFactory.h"

// Sender (decoupled component)
#include "sender/TelemetrySender.h"
#include "manager/TelemetryManager.h"
#include "utils/SerialCommands.h"



#ifndef BACKEND_BASE_URL
#define BACKEND_BASE_URL "https://your-backend.com"
#endif

#ifndef DEVICE_API_KEY
#define DEVICE_API_KEY "your-device-api-key"
#endif

IDataSource* dataSource = nullptr;
TelemetrySender* sender = nullptr;
TelemetryManager* manager = nullptr;

void setup() {
  Serial.begin(115200);
  delay(1000);
  
  Serial.println("\n========================================");
  Serial.println("ESP32 Telemetry System");
  Serial.println("========================================");
  
  String deviceId = DeviceIdentity::initialize();
  
  #ifdef DEVICE_ID
    String manualDeviceId = String(DEVICE_ID);
    if (manualDeviceId != "ESP32-DEFAULT" && 
        manualDeviceId != "your-device-api-key" && 
        manualDeviceId.length() > 0) {
      deviceId = manualDeviceId;
      Serial.printf("[DeviceIdentity] Using manually configured device ID: %s\n", deviceId.c_str());
    }
  #endif
  
  Serial.printf("Data Source Mode: %s\n", DataSourceFactory::getModeName());
  Serial.printf("Device ID: %s\n", deviceId.c_str());
  Serial.printf("Backend URL: %s\n", BACKEND_BASE_URL);
  Serial.println("========================================\n");

  initWiFi();
  
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WARNING: WiFi not connected. System will retry when WiFi is available.");
  }

  dataSource = DataSourceFactory::create();
  if (!dataSource) {
    Serial.println("ERROR: Failed to create data source!");
    return;
  }

  sender = new TelemetrySender(BACKEND_BASE_URL, deviceId, DEVICE_API_KEY);
  if (!sender) {
    Serial.println("ERROR: Failed to create telemetry sender!");
    return;
  }

  manager = new TelemetryManager(dataSource, sender);
  if (!manager) {
    Serial.println("ERROR: Failed to create telemetry manager!");
    return;
  }

  if (!manager->begin()) {
    Serial.println("ERROR: Failed to initialize telemetry manager!");
    return;
  }

  Serial.println("\nSystem initialized successfully!");
  Serial.println("Starting telemetry loop...\n");
}

void loop() {
  SerialCommands::process();
  
  if (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    return;
  }

  if (manager) {
    manager->process();
  } else {
    Serial.println("ERROR: Manager is null!");
    delay(5000);
    return;
  }

  delay(10);
  
  static unsigned long lastStatsPrint = 0;
  if (millis() - lastStatsPrint >= 30000) {
    lastStatsPrint = millis();
    Serial.println("\n--- Statistics ---");
    Serial.printf("Live sends: %lu\n", manager->getLiveSendCount());
    Serial.printf("History sends: %lu\n", manager->getHistorySendCount());
    Serial.printf("Errors: %lu\n", manager->getErrorCount());
    Serial.println("------------------\n");
  }
}
