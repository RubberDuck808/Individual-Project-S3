#pragma once
#include <Arduino.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include "../model/Telemetry.h"

class TelemetrySender {
public:
  TelemetrySender(const String& url, const String& apiKey)
    : url(url), apiKey(apiKey) {}

  bool send(const Telemetry& t);

private:
  String url;
  String apiKey;
};
