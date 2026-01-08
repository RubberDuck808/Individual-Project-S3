#include "TelemetrySender.h"

bool TelemetrySender::send(const Telemetry& t) {
  if (WiFi.status() != WL_CONNECTED) return false;

  HTTPClient http;
  http.begin(url);
  http.addHeader("Content-Type", "application/json");
  http.addHeader("X-API-Key", apiKey);

  String body;
  body.reserve(128);
  body += "{";
  body += "\"tsMs\":" + String(t.tsMs) + ",";
  body += "\"speedKph\":" + String(t.speedKph, 2) + ",";
  body += "\"rpm\":" + String(t.rpm, 0) + ",";
  body += "\"throttlePct\":" + String(t.throttlePct, 1);
  body += "}";

  int code = http.POST(body);
  String resp = http.getString();
  http.end();

  Serial.printf("POST %s -> %d\n", url.c_str(), code);
  if (code < 200 || code >= 300) {
    Serial.println(resp);
    return false;
  }
  return true;
}
