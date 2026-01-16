#pragma once
#include <Arduino.h>
#include <WiFiClient.h>
#include <WiFiClientSecure.h>
#include "../model/Telemetry.h"




/**
 * TelemetrySender - Decoupled component responsible ONLY for sending data to backend
 * 
 * This component:
 * - Handles HTTP/HTTPS communication
 * - Formats telemetry data as JSON
 * - Sends to appropriate backend endpoints (live/history)
 * - Manages authentication (API key)
 * - Does NOT know about data sources or business logic
 */
class TelemetrySender {
public:
  TelemetrySender(const String& backendUrl, const String& deviceId, const String& apiKey);
  
  /**
   * Send live telemetry (frequent updates for real-time map display)
   * PUT /api/telemetry/live
   */
  bool sendLive(const Telemetry& t);
  
  /**
   * Send historical telemetry (less frequent, for logging/analysis)
   * POST /api/telemetry/history
   */
  bool sendHistory(const Telemetry& t);
  
  /**
   * Check if sender is ready (WiFi connected, valid config)
   */
  bool isReady() const;

private:
  String backendUrl;
  String deviceId;
  String apiKey;
  
  /**
   * Internal method to send HTTP request
   */
  bool sendRequest(const String& endpoint, const String& method, const String& jsonBody);
  
  /**
   * Convert Telemetry to JSON for live endpoint
   */
  String toLiveJson(const Telemetry& t);
  
  /**
   * Convert Telemetry to JSON for history endpoint
   */
  String toHistoryJson(const Telemetry& t);
};
