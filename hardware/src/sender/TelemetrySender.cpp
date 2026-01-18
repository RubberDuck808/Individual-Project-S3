#include "TelemetrySender.h"
#include <HTTPClient.h>
#include <WiFi.h>

TelemetrySender::TelemetrySender(const String& backendUrl, const String& deviceId, const String& apiKey)
  : backendUrl(backendUrl), deviceId(deviceId), apiKey(apiKey) {}

bool TelemetrySender::isReady() const {
  return WiFi.status() == WL_CONNECTED &&
         backendUrl.length() > 0 &&
         deviceId.length() > 0 &&
         apiKey.length() > 0;
}

bool TelemetrySender::sendLive(const Telemetry& t) {
  if (!isReady()) {
    Serial.println("[TelemetrySender] Not ready (WiFi or config issue)");
    return false;
  }
  String jsonBody = toLiveJson(t);
  return sendRequest("/api/telemetry/live", "PUT", jsonBody);
}

bool TelemetrySender::sendHistory(const Telemetry& t) {
  if (!isReady()) {
    Serial.println("[TelemetrySender] Not ready (WiFi or config issue)");
    return false;
  }
  String jsonBody = toHistoryJson(t);
  return sendRequest("/api/telemetry/history", "POST", jsonBody);
}

bool TelemetrySender::sendRequest(const String& endpoint, const String& method, const String& jsonBody) {
  HTTPClient http;
  String fullUrl = backendUrl + endpoint;

  int httpCode = 0;
  String response;

  if (backendUrl.startsWith("https://")) {
    WiFiClientSecure client;
    client.setInsecure();

    if (!http.begin(client, fullUrl)) {
      Serial.printf("[TelemetrySender] Failed to begin HTTPS connection to %s\n", fullUrl.c_str());
      return false;
    }

    http.addHeader("Content-Type", "application/json");
    http.addHeader("X-API-Key", apiKey);

    if (method == "PUT") {
      httpCode = http.PUT(jsonBody);
    } else if (method == "POST") {
      httpCode = http.POST(jsonBody);
    } else {
      Serial.printf("[TelemetrySender] Unsupported HTTP method: %s\n", method.c_str());
      http.end();
      return false;
    }

    response = http.getString();
    http.end();

  } else if (backendUrl.startsWith("http://")) {
    WiFiClient client;

    if (!http.begin(client, fullUrl)) {
      Serial.printf("[TelemetrySender] Failed to begin HTTP connection to %s\n", fullUrl.c_str());
      return false;
    }

    http.addHeader("Content-Type", "application/json");
    http.addHeader("X-API-Key", apiKey);

    if (method == "PUT") {
      httpCode = http.PUT(jsonBody);
    } else if (method == "POST") {
      httpCode = http.POST(jsonBody);
    } else {
      Serial.printf("[TelemetrySender] Unsupported HTTP method: %s\n", method.c_str());
      http.end();
      return false;
    }

    response = http.getString();
    http.end();

  } else {
    Serial.printf("[TelemetrySender] Invalid backendUrl (must start with http:// or https://): %s\n",
                  backendUrl.c_str());
    return false;
  }

  if (httpCode >= 200 && httpCode < 300) {
    Serial.printf("[TelemetrySender] %s %s -> %d (OK)\n", method.c_str(), endpoint.c_str(), httpCode);
    return true;
  } else {
    Serial.printf("[TelemetrySender] %s %s -> %d (ERROR)\n", method.c_str(), endpoint.c_str(), httpCode);

    if (response.length() > 0) {
      Serial.printf("[TelemetrySender] Response: %s\n", response.c_str());
    }

    if (httpCode == 401) {
      Serial.println("[TelemetrySender] ERROR: Invalid API key. Check device registration.");
    } else if (httpCode == 429) {
      Serial.println("[TelemetrySender] WARNING: Rate limit exceeded. Slowing down...");
    }

    return false;
  }
}

String TelemetrySender::toLiveJson(const Telemetry& t) {
  String json;
  json.reserve(200);

  json += "{";
  json += "\"deviceId\":\"" + deviceId + "\",";
  json += "\"speedKph\":" + String(t.speedKph, 2) + ",";
  json += "\"rpm\":" + String(t.rpm, 0);
  json += "}";

  return json;
}

String TelemetrySender::toHistoryJson(const Telemetry& t) {
  String json;
  json.reserve(500);

  json += "{";
  json += "\"deviceId\":\"" + deviceId + "\",";
  json += "\"speedKph\":" + String(t.speedKph, 2) + ",";
  json += "\"rpm\":" + String(t.rpm, 0) + ",";
  json += "\"throttlePct\":" + String(t.throttlePct, 1);

  if (t.coolantTempC != 0.0f) {
    json += ",\"coolantTempC\":" + String(t.coolantTempC, 1);
  }
  if (t.batteryVoltageV != 0.0f) {
    json += ",\"batteryVoltageV\":" + String(t.batteryVoltageV, 2);
  }
  if (t.oilTempC != 0.0f) {
    json += ",\"oilTempC\":" + String(t.oilTempC, 1);
  }
  if (t.fuelLevelPct != 0.0f) {
    json += ",\"fuelLevelPct\":" + String(t.fuelLevelPct, 1);
  }
  if (t.intakeAirTempC != 0.0f) {
    json += ",\"intakeAirTempC\":" + String(t.intakeAirTempC, 1);
  }
  if (t.engineLoadPct != 0.0f) {
    json += ",\"engineLoadPct\":" + String(t.engineLoadPct, 1);
  }
  if (t.mafAirFlow != 0.0f) {
    json += ",\"mafAirFlow\":" + String(t.mafAirFlow, 2);
  }
  if (t.mapPressure != 0.0f) {
    json += ",\"mapPressure\":" + String(t.mapPressure, 2);
  }
  if (t.timingAdvance != 0.0f) {
    json += ",\"timingAdvance\":" + String(t.timingAdvance, 1);
  }
  if (t.diagnosticCodes.length() > 0) {
    json += ",\"diagnosticCodes\":\"" + t.diagnosticCodes + "\"";
  }

  json += "}";

  return json;
}
