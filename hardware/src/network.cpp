#include <Arduino.h>
#include <WiFi.h>
#include "network.h"
#include "secrets.h"

void initWiFi() {
    Serial.println("Starting WiFi...");
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASS);

    while (WiFi.status() != WL_CONNECTED) {
        Serial.println("Connecting to WiFi...");
        delay(500);
    }

    Serial.println("WiFi connected!");
    Serial.println(WiFi.localIP());
}
