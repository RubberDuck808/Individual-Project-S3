#include <Arduino.h>
#include "secrets.h"

#include "./network/network.h"
#include "data/SimulatedDataSource.h"
#include "sender/TelemetrySender.h"

static const char* TELEMETRY_URL = "https://your-backend/api/telemetry";
static const char* API_KEY = "your-device-key";

// Swap this later to ObdCanDataSource
SimulatedDataSource dataSource;
TelemetrySender sender(TELEMETRY_URL, API_KEY);

unsigned long lastSend = 0;

void setup() {
  Serial.begin(115200);
  delay(1000);

  initWiFi();

  if (!dataSource.begin()) {
    Serial.println("Data source failed to init!");
  }

  Serial.println("System initialized.");
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    // optional: reconnect logic here
    delay(500);
    return;
  }

  if (millis() - lastSend >= 1000) { // send every second
    lastSend = millis();

    Telemetry t{};
    if (dataSource.read(t)) {
      sender.send(t);
    }
  }
}
